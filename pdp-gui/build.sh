#!/bin/bash
rm -rf dist && rm -rf tmp && rm -rf build
npm install
npm run lint
npm run webpack