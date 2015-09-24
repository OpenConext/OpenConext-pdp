/** @jsx React.DOM */

App.Mixins.SortableTable = function(localeKey, attr, sortAscending) {
  var converterForAttribute = function(attr) {
    var attrName = attr.charAt(0).toUpperCase() + attr.slice(1);
    return this["convert" + attrName + "ForSort"];
  }

  var compare = function(a, b) {
    if (a < b) {
      return -1;
    } else if (a > b) {
      return 1;
    }
    return 0;
  }

  return {
    getInitialState: function() {
      return {
        sortAttribute: attr,
        sortAscending: sortAscending
      }
    },

    sort: function(list) {
      return list.sort(function(a, b) {
        var aAttr = a[this.state.sortAttribute];
        var bAttr = b[this.state.sortAttribute];

        var converter = converterForAttribute.call(this, this.state.sortAttribute);
        if (converter) {
          aAttr = converter.call(this, aAttr, a);
          bAttr = converter.call(this, bAttr, b);
        }

        var result = compare(aAttr, bAttr);

        if (this.state.sortAscending) {
          return result * -1;
        }
        return result;
      }.bind(this));
    },

    renderSortableHeader: function(className, attribute) {
      if (this.state.sortAttribute == attribute) {
        var icon = this.renderSortDirection();
      } else {
        var icon = <i className="fa fa-sort"></i>;
      }

      return (
        <th className={className}>
          <a href="#" onClick={this.handleSort(attribute)}>
            {I18n.t(localeKey + "." + attribute)}
            {icon}
          </a>
        </th>
      );
    },

    renderSortDirection: function() {
      if (this.state.sortAscending) {
        return <i className="fa fa-sort-asc"></i>;
      } else {
        return <i className="fa fa-sort-desc"></i>;
      }
    },

    handleSort: function(attribute) {
      return function(e) {
        e.preventDefault();
        e.stopPropagation();
        if (this.state.sortAttribute == attribute) {
          this.setState({sortAscending: !this.state.sortAscending});
        } else {
          this.setState({
            sortAttribute: attribute,
            sortAscending: false
          });
        }
      }.bind(this);
    }
  }
}
