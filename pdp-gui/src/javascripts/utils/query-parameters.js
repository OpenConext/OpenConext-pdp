const QueryParameter = {

  //shameless refactor of https://gist.githubusercontent.com/pduey/2764606/raw/e8b9d6099f1e4161f7dd9f81d71c2c7a1fecbd5b/querystring.js

  searchToHash: function() {
    const h = {};
    if (window.location.search === undefined || window.location.search.length < 1) {
      return h;
    }
    const q = window.location.search.slice(1).split("&");
    for (let i = 0; i < q.length; i++) {
      const keyVal = q[i].split("=");
      // replace '+' (alt space) char explicitly since decode does not
      const hkey = decodeURIComponent(keyVal[0]).replace(/\+/g, " ");
      const hval = decodeURIComponent(keyVal[1]).replace(/\+/g, " ");
      if (h[hkey] === undefined) {
        h[hkey] = [];
      }
      h[hkey].push(hval);
    }
    return h;
  },


  hashToSearch: function(h) {
    let search = "?";
    for (const k in h) {
      if (k.hasOwnProperty(h)) {
        for (let i = 0; i < h[k].length; i++) {
          search += search === "?" ? "" : "&";
          search += encodeURIComponent(k) + "=" + encodeURIComponent(h[k][i]);
        }
      }
    }
    return search;
  },

  replaceQueryParameter: function(name, value) {
    const newSearchHash = this.searchToHash();
    delete newSearchHash[name];
    newSearchHash[decodeURIComponent(name)] = [decodeURIComponent(value)];
    return this.hashToSearch(newSearchHash);
  },

  addQueryParameter: function(name, value) {
    const newSearchHash = this.searchToHash();
    if (!(decodeURIComponent(name) in newSearchHash)) {
      newSearchHash[decodeURIComponent(name)] = [];
    }
    newSearchHash[decodeURIComponent(name)].push(decodeURIComponent(value));
    return this.hashToSearch(newSearchHash);
  },

  removeQueryParameter: function(name, value) {
    const newSearchHash = this.searchToHash();
    if (newSearchHash[name] && newSearchHash[name].indexOf(value) > -1) {
      newSearchHash[name].splice(newSearchHash[name].indexOf(value), 1);
      if (newSearchHash[name].length === 0) {
        delete newSearchHash[name];
      }
    }
    return this.hashToSearch(newSearchHash);
  },

  getParameterByName: function(name) {
    const replacedName = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    const regex = new RegExp("[\\?&]" + replacedName + "=([^&#]*)"),
      results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
  }
};

export default QueryParameter;
