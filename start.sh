#!/bin/bash

echo "🔄 Nettoyage des conteneurs précédents..."
docker-compose down

echo "🧹 Suppression des images orphelines (dangling)..."
docker image prune -f

echo "🐳 Construction des conteneurs (backend + frontend)..."
docker-compose build --no-cache

echo "🚀 Lancement de l'application..."
docker-compose up
