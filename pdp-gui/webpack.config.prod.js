/* eslint-disable */

const path = require("path");
const glob = require("glob");
const webpack = require("webpack");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const OptimizeCssAssetsPlugin = require("optimize-css-assets-webpack-plugin");
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');

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
            {test: /\.s(c|a)ss$/, loader: extractCSS.extract("style", "css?!sass?!import-glob")},
            {test: /\.css$/, loader: extractCSS.extract("style", "css?")},
            {test: /\.(png|jpg)$/, loader: "file"},
            {test: /\.((woff2?|svg)(\?v=[0-9]\.[0-9]\.[0-9]))|(woff2?|svg)$/, loader: "url?limit=10000"},
            {test: /\.((ttf|eot)(\?v=[0-9]\.[0-9]\.[0-9]))|(ttf|eot)$/, loader: "file"},
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: "babel",
                query: {
                    presets: ["stage-0", "react", "es2015"],
                    plugins: ["transform-object-rest-spread", "lodash"]
                },
            },
        ]
    },

    plugins: [
        extractCSS,
        new HtmlWebpackPlugin({
            template: "src/index.html.ejs",
            favicon: "src/favicon.ico",
            hash: true
        }),
        new OptimizeCssAssetsPlugin({
            assetNameRegExp: /\.css$/,
            cssProcessorOptions: { discardComments: { removeAll: true } }
        }),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: JSON.stringify('production')
            }
        }),
        new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en|nl/)
        //new UglifyJSPlugin()
    ]
};
