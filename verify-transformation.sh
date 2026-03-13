#!/bin/bash

echo "================================"
echo "Pastely Transformation Verification"
echo "================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (missing)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (missing)"
        return 1
    fi
}

echo "Backend Files:"
check_file "backend/src/main/java/cc/allyapps/pastely/Pastely.java"
check_file "backend/src/main/java/cc/allyapps/pastely/helper/JarvisClient.java"
check_file "backend/src/main/java/cc/allyapps/pastely/ai/PasteAI.java"
check_file "backend/src/main/java/cc/allyapps/pastely/controller/PasteAIController.java"
check_dir "backend/src/main/java/cc/allyapps/pastely/model/requests/ai"
check_dir "backend/src/main/java/cc/allyapps/pastely/model/responses/ai"
check_file "backend/.env.example"
check_file "backend/pom.xml"

echo ""
echo "Frontend Files:"
check_file "frontend/src/types/ai.ts"
check_file "frontend/src/stores/ai.ts"
check_file "frontend/index.html"
check_file "frontend/public/manifest.json"
check_file "frontend/public/icons/logo-dark.svg"
check_file "frontend/public/icons/logo-light.svg"

echo ""
echo "Assets:"
check_file ".github/logo-black.svg"
check_file ".github/logo-white.svg"

echo ""
echo "Documentation:"
check_file "README.md"
check_file "PASTELY_FEATURES.md"
check_file "QUICKSTART_PASTELY.md"
check_file "IMPLEMENTATION_SUMMARY.md"
check_file "PASTELY_TRANSFORMATION_COMPLETE.md"

echo ""
echo "Package Verification:"
echo -n "Checking for old package references... "
OLD_REFS=$(grep -r "de.interaapps.pastefy" backend/src --include="*.java" 2>/dev/null | wc -l)
if [ "$OLD_REFS" -eq 0 ]; then
    echo -e "${GREEN}✓ None found${NC}"
else
    echo -e "${RED}✗ Found $OLD_REFS references${NC}"
fi

echo -n "Checking for Anthropic references... "
ANTHROPIC_REFS=$(grep -r "anthropic" backend/src --include="*.java" -i 2>/dev/null | wc -l)
if [ "$ANTHROPIC_REFS" -eq 0 ]; then
    echo -e "${GREEN}✓ None found${NC}"
else
    echo -e "${RED}✗ Found $ANTHROPIC_REFS references${NC}"
fi

echo ""
echo "Configuration Verification:"
echo -n "Checking .env.example for Jarvis config... "
if grep -q "JARVIS_GATEWAY_URL" backend/.env.example && \
   grep -q "JARVIS_GATEWAY_TOKEN" backend/.env.example; then
    echo -e "${GREEN}✓ Found${NC}"
else
    echo -e "${RED}✗ Missing${NC}"
fi

echo ""
echo "Build Verification:"
echo -n "Checking pom.xml artifact ID... "
if grep -q "<artifactId>pastely-core</artifactId>" backend/pom.xml; then
    echo -e "${GREEN}✓ pastely-core${NC}"
else
    echo -e "${RED}✗ Not updated${NC}"
fi

echo ""
echo "================================"
echo "Verification Complete!"
echo "================================"
