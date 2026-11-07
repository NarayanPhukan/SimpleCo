/**
 * BabyyGurll Boutique - Automated Tag Analyzer
 * Analyzes category, subcategory, title, and description to extract high-relevance tags.
 */

function generateAutomatedTags({
  category = '',
  categoryName = '',
  subcategory = '',
  subcategoryName = '',
  name = '',
  description = ''
}) {
  const fullText = [category, categoryName, subcategory, subcategoryName, name, description]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();

  const tags = new Set();

  // 1. Core artisanal crochet tags
  tags.add('crochet');
  tags.add('handmade');

  const hasWord = (word) => {
    const escaped = word.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(?:^|[^a-z0-9])${escaped}(?:$|[^a-z0-9])`, 'i');
    return regex.test(fullText);
  };

  // 2. High-intent exact phrases
  const phraseMatches = [
    'triangle bandana', 'hair scarf', 'head scarf', 'hair accessory', 'hair accessories',
    'tote bag', 'shoulder bag', 'crossbody bag', 'granny square', 'milk cotton',
    'gift for her', 'desk decor', 'home decor', 'wall hanging', 'mug rug',
    'bucket hat', 'crop top', 'cushion cover', 'bag charm', 'plushie keychain',
    'strawberry cow', 'forever flowers', 'open knit', 'cottagecore', 'cozy'
  ];
  for (const phrase of phraseMatches) {
    if (fullText.includes(phrase)) tags.add(phrase);
  }

  // 3. Keyword to tags mapping
  const keywordMap = {
    'bandana': ['bandana', 'headwear'],
    'headscarf': ['headscarf', 'hair scarf'],
    'scrunchie': ['scrunchie', 'hair tie', 'hair accessory'],
    'plush': ['plushie', 'amigurumi'],
    'plushie': ['plushie', 'amigurumi'],
    'amigurumi': ['amigurumi', 'plushie'],
    'bag': ['crochet bag'],
    'tote': ['tote bag', 'crochet bag'],
    'purse': ['purse', 'crochet bag'],
    'tulip': ['tulip', 'floral', 'flower bouquet'],
    'rose': ['rose', 'floral', 'flower bouquet'],
    'sunflower': ['sunflower', 'floral'],
    'daisy': ['daisy', 'floral'],
    'bouquet': ['bouquet', 'forever flowers'],
    'flower': ['floral', 'flowers'],
    'bloom': ['floral'],
    'coaster': ['coaster', 'tableware', 'home decor'],
    'cardigan': ['cardigan', 'apparel', 'knitwear'],
    'sweater': ['sweater', 'apparel'],
    'vest': ['vest', 'apparel'],
    'scarf': ['scarf'],
    'beanie': ['beanie', 'winter hat'],
    'beret': ['beret'],
    'hat': ['crochet hat'],
    'keychain': ['keychain', 'bag charm'],
    'cow': ['cow plush', 'cute animal'],
    'bunny': ['bunny plush', 'cute animal'],
    'cat': ['cat plush', 'cute animal'],
    'dog': ['dog plush', 'cute animal'],
    'boho': ['boho', 'bohemian'],
    'bohemian': ['boho', 'bohemian'],
    'vintage': ['vintage'],
    'cotton': ['milk cotton'],
    'chenille': ['chenille yarn'],
    'wool': ['wool'],
    'aesthetic': ['aesthetic'],
    'pastel': ['pastel'],
    'retro': ['retro'],
    'triangle': ['triangle bandana'],
    'summer': ['summer style'],
    'beach': ['beach wear'],
    'gift': ['gift for her']
  };

  for (const [key, mapped] of Object.entries(keywordMap)) {
    if (hasWord(key)) {
      mapped.forEach(t => tags.add(t));
    }
  }

  // 4. Subcategory-specific enrichment
  const sub = (subcategoryName || subcategory || '').toLowerCase();
  if (sub.includes('bandana')) { tags.add('bandana'); tags.add('headwear'); tags.add('hair accessories'); }
  if (sub.includes('scrunchie')) { tags.add('scrunchie'); tags.add('hair accessories'); }
  if (sub.includes('tote')) { tags.add('tote bag'); tags.add('crochet bag'); }
  if (sub.includes('shoulder')) { tags.add('shoulder bag'); tags.add('crochet bag'); }
  if (sub.includes('hat') || sub.includes('beanie')) { tags.add('crochet hat'); }
  if (sub.includes('plush') || sub.includes('amigurumi')) { tags.add('amigurumi'); tags.add('plushie'); }
  if (sub.includes('bouquet') || sub.includes('flower')) { tags.add('forever flowers'); tags.add('floral'); }
  if (sub.includes('coaster') || sub.includes('rug')) { tags.add('home decor'); tags.add('coaster'); }
  if (sub.includes('cardigan') || sub.includes('sweater') || sub.includes('top')) { tags.add('knitwear'); tags.add('apparel'); }

  // 5. Unique title tokens
  const stopWords = new Set(['with', 'from', 'this', 'that', 'your', 'made', 'item', 'boutique', 'babyygurll', 'piece', 'hand']);
  const titleTokens = (name || '').toLowerCase()
    .replace(/[^a-z0-9\s]/g, ' ')
    .split(/\s+/)
    .filter(w => w.length >= 4 && !stopWords.has(w) && w !== 'crochet' && w !== 'handmade' && w !== 'handcrafted');

  for (const token of titleTokens) {
    tags.add(token);
  }

  return Array.from(tags).filter(Boolean).slice(0, 15);
}

module.exports = {
  generateAutomatedTags
};
