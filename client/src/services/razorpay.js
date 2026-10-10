import { api } from './api';

// Ensures https://checkout.razorpay.com/v1/checkout.js is loaded
export const loadRazorpayScript = () => {
  return new Promise((resolve) => {
    if (window.Razorpay) {
      resolve(true);
      return;
    }
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
};

/**
 * Initiates Razorpay Standard Web Checkout
 * @param {Object} params
 * @param {number} params.amountInRupees - Total amount in INR
 * @param {Object} params.prefill - { name, email, contact }
 * @param {Function} params.onSuccess - Callback on verified payment (verifiedData)
 * @param {Function} params.onError - Callback on payment failure or verification failure (errorMessage)
 * @param {Function} params.onDismiss - Callback when user closes payment modal
 */
export const openRazorpayStandardCheckout = async ({
  amountInRupees,
  prefill = {},
  onSuccess,
  onError,
  onDismiss
}) => {
  try {
    // 1. Ensure Razorpay Checkout script is loaded
    const isLoaded = await loadRazorpayScript();
    if (!isLoaded || !window.Razorpay) {
      throw new Error('Razorpay SDK failed to load. Please check your internet connection.');
    }

    // 2. Amount in paise (1 INR = 100 paise)
    const amountInPaise = Math.round(Number(amountInRupees) * 100);
    if (amountInPaise < 100) {
      throw new Error('Order amount must be at least ₹1.00 (100 paise).');
    }

    // 3. Call backend to create Razorpay Order
    const orderRes = await api.createRazorpayOrder({
      amount: amountInPaise,
      currency: 'INR',
      receipt: `rcpt_${Date.now()}`
    });

    const orderId = orderRes.order_id || orderRes.id;
    const razorpayKey = orderRes.key_id || (await api.getRazorpayKey()) || import.meta.env.VITE_RAZORPAY_KEY_ID;

    if (!orderId) {
      throw new Error('Could not retrieve Razorpay Order ID from server.');
    }
    if (!razorpayKey) {
      throw new Error('Razorpay Key ID is missing. Please configure RAZORPAY_KEY_ID in .env.');
    }

    // 4. Configure Razorpay Standard Checkout Options
    const options = {
      key: razorpayKey,
      amount: orderRes.amount,
      currency: orderRes.currency || 'INR',
      name: 'BabyyGurll Crochet',
      description: 'Artisanal Handmade Crochet Order',
      image: '/assets/images/strawberry-cow.jpg',
      order_id: orderId,
      prefill: {
        name: prefill.name || '',
        email: prefill.email || '',
        contact: prefill.contact || ''
      },
      notes: {
        store: 'BabyyGurll Crochet India',
        category: 'Artisanal Handcrafts'
      },
      theme: {
        color: '#FF7B9C' // Brand signature pastel pink/rose
      },
      modal: {
        ondismiss: function () {
          if (onDismiss) onDismiss();
        }
      },
      handler: async function (response) {
        // Step 3: Send payment details to backend for signature verification
        try {
          const verifyRes = await api.verifyRazorpayPayment({
            razorpay_order_id: response.razorpay_order_id,
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_signature: response.razorpay_signature
          });

          if (verifyRes.verified || verifyRes.success) {
            if (onSuccess) {
              onSuccess({
                paymentId: response.razorpay_payment_id,
                orderId: response.razorpay_order_id,
                signature: response.razorpay_signature,
                verifiedResponse: verifyRes
              });
            }
          } else {
            throw new Error(verifyRes.message || 'Payment signature verification failed.');
          }
        } catch (verifyErr) {
          if (onError) onError(verifyErr.message || 'Payment verification failed on server.');
        }
      }
    };

    const rzp = new window.Razorpay(options);

    rzp.on('payment.failed', function (response) {
      const errorDesc = response.error?.description || response.error?.reason || 'Payment failed';
      if (onError) onError(`Payment failed: ${errorDesc}`);
    });

    rzp.open();
  } catch (err) {
    if (onError) onError(err.message || 'An error occurred initializing payment.');
  }
};
