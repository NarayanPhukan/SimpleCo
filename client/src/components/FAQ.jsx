import React, { useState } from 'react';

export const FAQ = () => {
  const [openIndex, setOpenIndex] = useState(null);

  const faqs = [
    {
      q: "How do I wash and look after my crochet piece?",
      a: "Gently soak it in cool water with a tiny drop of mild shampoo or baby wash. Never wring or twist it! Just press out the extra water on a clean towel and lay it flat in the shade to dry so it keeps its cute shape."
    },
    {
      q: "How long does each piece take to reach me?",
      a: "Ready-to-ship pieces get packed and sent out within 1–2 days. For made-to-order items (like bouquets and totes), please give us 3–5 days to hand-hook them with love. You can track your parcel's progress right here on the site!"
    },
    {
      q: "Can I request custom colors or a gift message?",
      a: "Yes, we'd love that! Just add a note at checkout with your favorite shades or what you'd like written on the gift card, and we'll personalize it for free."
    },
    {
      q: "What happens if something arrives damaged?",
      a: "Because everything is handmade, we don't take returns for simple change-of-mind. But if anything arrives damaged or not as described, reach out right away and we will happily remake it or give you a full refund."
    }
  ];

  const toggle = (idx) => {
    setOpenIndex(openIndex === idx ? null : idx);
  };

  return (
    <section className="faq-section">
      <div className="container">
        <div className="section-header">
          <span className="section-tag">Common Questions 💬</span>
          <h2 className="section-title">Things people ask us often</h2>
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
