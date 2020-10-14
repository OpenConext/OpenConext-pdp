#!/bin/bash
rm -rf dist && rm -rf tmp && rm -rf build
yarn install
yarn run lint
yarn run webpack
