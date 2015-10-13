App.Utils.QueryParameter = {

  //shameless refactor of https://gist.githubusercontent.com/pduey/2764606/raw/e8b9d6099f1e4161f7dd9f81d71c2c7a1fecbd5b/querystring.js

  searchToHash: function () {
    var h = {};
    if (window.location.search == undefined || window.location.search.length < 1) {
      return h;
    }
    var q = window.location.search.slice(1).split('&');
    for (var i = 0; i < q.length; i++) {
      var keyVal = q[i].split('=');
      // replace '+' (alt space) char explicitly since decode does not
      var hkey = decodeURIComponent(keyVal[0]).replace(/\+/g, ' ');
      var hval = decodeURIComponent(keyVal[1]).replace(/\+/g, ' ');
      if (h[hkey] == undefined) {
        h[hkey] = [];
      }
      h[hkey].push(hval);
    }
    return h;
  },


  hashToSearch: function (h) {
    var search = "?";
    for (var k in h) {
      for (var i = 0; i < h[k].length; i++) {
        search += search == "?" ? "" : "&";
        search += encodeURIComponent(k) + "=" + encodeURIComponent(h[k][i]);
      }
    }
    return search;
  },

  replaceQueryParameter: function(name, value) {
    var newSearchHash = this.searchToHash();
    delete newSearchHash[name];
    newSearchHash[decodeURIComponent(name)] = [decodeURIComponent(value)];
    return this.hashToSearch(newSearchHash);
  },

  addQueryParameter: function (name, value) {
    var newSearchHash = this.searchToHash();
    if (!(decodeURIComponent(name) in newSearchHash)) {
      newSearchHash[decodeURIComponent(name)] = [];
    }
    newSearchHash[decodeURIComponent(name)].push(decodeURIComponent(value));
    return this.hashToSearch(newSearchHash);
  },

  removeQueryParameter: function (name, value) {
    var newSearchHash = this.searchToHash();
    if (newSearchHash[name] && newSearchHash[name].indexOf(value) > -1) {
      newSearchHash[name].splice(newSearchHash[name].indexOf(value), 1);
      if (newSearchHash[name].length == 0) {
        delete newSearchHash[name];
      }
    }
    return this.hashToSearch(newSearchHash);
  },

  getParameterByName: function (name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
  }
};
