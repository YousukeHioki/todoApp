#!/bin/bash
npm run build --prefix frontend
cp -rf frontend/dist/* backend/src/main/resources/static
