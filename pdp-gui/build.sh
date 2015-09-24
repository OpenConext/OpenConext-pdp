#!/bin/bash
rm -Rf dist/*
npm install
./node_modules/grunt-cli/bin/grunt prod
