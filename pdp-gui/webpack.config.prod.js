/* eslint-disable */

const path = require("path");
const glob = require("glob");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const ExtractTextPlugin = require("extract-text-webpack-plugin");

const extractCSS = new ExtractTextPlugin("application-[contenthash].css");

const PATHS = {
  app: path.join(__dirname, "src/javascripts"),
  build: path.join(__dirname, "dist")
};

module.exports = {
  entry: {
    app: ["babel-polyfill", PATHS.app]
  },

  output: {
    filename: "application-[hash].js", // Template based on keys in entry above
    path: PATHS.build, // This is where images AND js will go
    publicPath: "/"
  },

  resolve: {
    root: path.resolve(PATHS.app),
    extensions: ["", ".js", ".jsx"]
  },

  module: {
    loaders: [
      { test: /\.s(c|a)ss$/, loader: extractCSS.extract("style", "css?sourceMap!sass?sourceMap!import-glob") },
      { test: /\.css$/, loader: extractCSS.extract("style?sourceMap", "css?sourceMap") },
      { test: /\.(png|jpg)$/, loader: "file" },
      { test: /\.((woff2?|svg)(\?v=[0-9]\.[0-9]\.[0-9]))|(woff2?|svg)$/, loader: "url?limit=10000" },
      { test: /\.((ttf|eot)(\?v=[0-9]\.[0-9]\.[0-9]))|(ttf|eot)$/, loader: "file" },
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: "babel",
        query: {
          presets: ["react", "es2015"],
          plugins: ["transform-object-rest-spread"]
        },
      },
    ]
  },

  devtool: "cheap-module-eval-source-map",
  devServer: {
    contentBase: PATHS.build,
    historyApiFallback: true,
    hot: true,
    inline: true,
    progress: true,
    stats: "errors-only",
    port: 8001,
    proxy: [
      {
        context: function(pathname, req) {
          return pathname.match("^/dashboard");
        },
        target: {
          port: 8280
        }
      }
    ]
  },
  plugins: [
    extractCSS,
    new webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin({
      template: "src/index.html.ejs"
    })
  ]
};
