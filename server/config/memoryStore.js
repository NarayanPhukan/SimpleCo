// Embedded In-Memory MongoDB Engine for BabyyGurll Crochet
// Allows the MERN backend to run out-of-the-box without requiring an external mongod binary or shared library dependencies

function enhanceAddressesArray(arr) {
  arr.id = function(subId) {
    return this.find(item => (item._id && item._id.toString() === subId.toString()) || item.id === subId) || null;
  };
  arr.pull = function(criteria) {
    const targetId = criteria._id ? criteria._id.toString() : criteria.toString();
    const idx = this.findIndex(item => (item._id && item._id.toString() === targetId) || item.id === targetId);
    if (idx > -1) {
      this.splice(idx, 1);
    }
    return this;
  };
}

class MemoryDocument {
  constructor(data, collection) {
    Object.assign(this, JSON.parse(JSON.stringify(data)));
    if (!this._id) {
      this._id = 'doc_' + Math.random().toString(36).substring(2, 9);
    }
    
    // Store non-enumerable reference to avoid circular JSON serialization in Express
    Object.defineProperty(this, '_collection', {
      value: collection,
      enumerable: false,
      writable: true,
      configurable: true
    });

    if (Array.isArray(this.savedAddresses)) {
      this.savedAddresses = this.savedAddresses.map(addr => {
        if (!addr._id) addr._id = 'addr_' + Math.random().toString(36).substring(2, 9);
        return addr;
      });
      enhanceAddressesArray(this.savedAddresses);
    }
  }

  toJSON() {
    const clone = { ...this };
    delete clone._collection;
    return clone;
  }

  async save() {
    if (this._collection) {
      const idx = this._collection.data.findIndex(d => d._id === this._id);
      if (idx > -1) {
        this._collection.data[idx] = this;
      } else {
        this._collection.data.push(this);
      }
    }
    return this;
  }
}

class MemoryCollection {
  constructor(initialData = []) {
    this.data = initialData.map(item => new MemoryDocument(item, this));
  }

  _match(doc, query) {
    if (!query || Object.keys(query).length === 0) return true;

    for (const key of Object.keys(query)) {
      if (key === '$or') {
        const orConditions = query['$or'];
        const matchedOr = orConditions.some(cond => this._match(doc, cond));
        if (!matchedOr) return false;
        continue;
      }

      const queryVal = query[key];
      const docVal = doc[key];

      if (queryVal && typeof queryVal === 'object' && queryVal.$regex) {
        const flags = queryVal.$options || '';
        const regex = new RegExp(queryVal.$regex, flags);
        if (!regex.test(String(docVal || ''))) return false;
      } else {
        if (String(docVal || '').toLowerCase() !== String(queryVal || '').toLowerCase()) {
          return false;
        }
      }
    }
    return true;
  }

  find(query = {}) {
    const results = this.data.filter(doc => this._match(doc, query)).map(d => new MemoryDocument(d, this));

    // Provide chainable .sort()
    const promise = Promise.resolve(results);
    promise.sort = (sortOptions = {}) => {
      const sorted = [...results];
      for (const field of Object.keys(sortOptions)) {
        const order = sortOptions[field]; // 1 or -1
        sorted.sort((a, b) => {
          if (a[field] < b[field]) return -1 * order;
          if (a[field] > b[field]) return 1 * order;
          return 0;
        });
      }
      return Promise.resolve(sorted);
    };

    return promise;
  }

  async findOne(query = {}) {
    const found = this.data.find(doc => this._match(doc, query));
    return found ? new MemoryDocument(found, this) : null;
  }

  async findById(id) {
    const found = this.data.find(doc => doc._id === id || doc.id === id);
    return found ? new MemoryDocument(found, this) : null;
  }

  async create(docData) {
    const doc = new MemoryDocument(docData, this);
    this.data.push(doc);
    return doc;
  }

  async insertMany(docs) {
    const created = docs.map(d => new MemoryDocument(d, this));
    this.data.push(...created);
    return created;
  }

  async countDocuments(query = {}) {
    return this.data.filter(doc => this._match(doc, query)).length;
  }
}

module.exports = { MemoryCollection, MemoryDocument, enhanceAddressesArray };
