#!/bin/bash

# Script de vérification de l'insertion des agences
# Usage: ./verify-agences.sh

echo "🔍 Vérification des agences dans la base de données..."
echo ""

# Couleurs pour le terminal
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Vérifier le nombre total d'agences
echo -e "${BLUE}📊 Nombre total d'agences :${NC}"
PGPASSWORD=1234 psql -U admingesco -d gesco -t -c "SELECT COUNT(*) FROM agences;"
echo ""

# 2. Agences par région
echo -e "${BLUE}📍 Répartition par région :${NC}"
PGPASSWORD=1234 psql -U admingesco -d gesco -c "SELECT region, COUNT(*) AS nombre FROM agences GROUP BY region ORDER BY nombre DESC;"
echo ""

# 3. Exemples d'agences
echo -e "${BLUE}📋 Exemples d'agences (10 premières) :${NC}"
PGPASSWORD=1234 psql -U admingesco -d gesco -c "SELECT code, label, region, num_tel FROM agences ORDER BY region, label LIMIT 10;"
echo ""

# 4. Vérifier les agences sans téléphone
echo -e "${BLUE}⚠️  Agences sans numéro de téléphone :${NC}"
NO_TEL=$(PGPASSWORD=1234 psql -U admingesco -d gesco -t -c "SELECT COUNT(*) FROM agences WHERE num_tel IS NULL;")
echo "Total: $NO_TEL agences"
echo ""

# 5. Vérifier le backend Spring Boot
echo -e "${BLUE}🔧 Vérification du backend (port 8081) :${NC}"
if lsof -ti:8081 > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Backend Spring Boot est en cours d'exécution${NC}"
    PID=$(lsof -ti:8081)
    echo "   PID: $PID"
else
    echo -e "${YELLOW}⚠️  Backend Spring Boot n'est pas en cours d'exécution${NC}"
    echo "   Démarrez-le avec: cd TriCoBack && ./mvnw spring-boot:run"
fi
echo ""

# 6. Vérifier le frontend Angular
echo -e "${BLUE}🎨 Vérification du frontend (port 4200) :${NC}"
if lsof -ti:4200 > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Frontend Angular est en cours d'exécution${NC}"
    PID=$(lsof -ti:4200)
    echo "   PID: $PID"
else
    echo -e "${YELLOW}⚠️  Frontend Angular n'est pas en cours d'exécution${NC}"
    echo "   Démarrez-le avec: cd Tri-ui && npm start"
fi
echo ""

# 7. Afficher les agences les plus récentes
echo -e "${BLUE}🆕 5 dernières agences insérées :${NC}"
PGPASSWORD=1234 psql -U admingesco -d gesco -c "SELECT id, code, label, region FROM agences ORDER BY id DESC LIMIT 5;"
echo ""

echo -e "${GREEN}✅ Vérification terminée !${NC}"
echo ""
echo "🌐 Pour voir les agences dans l'interface :"
echo "   1. Ouvrez http://localhost:4200"
echo "   2. Connectez-vous avec un compte admin ou superviseur"
echo "   3. Cliquez sur 'Agences' dans le menu latéral"
