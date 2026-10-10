import React, { useState } from 'react';

export const FAQ = () => {
  const [openIndex, setOpenIndex] = useState(null);

  const faqs = [
    {
      q: "How do I wash and care for my crochet items?",
      a: "We recommend gentle hand washing in cool water with mild wool detergent. Lay flat on a clean dry towel to air dry. Do not tumble dry, bleach, or wring out yarn fibers to preserve stitch elasticity."
    },
    {
      q: "How long does artisanal handcrafting take?",
      a: "Items marked 'Ready to ship' depart our studio within 1-2 business days. Custom or made-to-order creations (like cardigans and large bouquets) take 3-5 crafting days. You can track your order status live at any point!"
    },
    {
      q: "Can I request custom colors or personalized name tags?",
      a: "Yes, absolutely! During checkout or in the product customizer, you can add personalization requests or select from our seasonal pastel yarn palette."
    },
    {
      q: "What is your return policy?",
      a: "We want you to adore your purchase! If there is any defect or damage upon arrival, contact us within 14 days and we will gladly arrange a remake or full refund."
    }
  ];

  const toggle = (idx) => {
    setOpenIndex(openIndex === idx ? null : idx);
  };

  return (
    <section className="faq-section">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Got Questions?</span>
          <h2 className="section-title">Frequently Asked Questions</h2>
        </div>

        <div className="faq-accordion">
          {faqs.map((item, idx) => (
            <div key={idx} className={`faq-item ${openIndex === idx ? 'open' : ''}`}>
              <button className="faq-question" onClick={() => toggle(idx)}>
                <span>{item.q}</span>
                <span className="faq-icon">+</span>
              </button>
              <div className="faq-answer">
                {item.a}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
