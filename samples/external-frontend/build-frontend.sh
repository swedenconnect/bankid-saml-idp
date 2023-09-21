# This script builds an nginx pod with static content of our frontend

# 1. Fetch Sources
#
# We choose to copy since we are in the same git repository
# We recommend you to do a spare-checkout on tagged releases
mkdir frontend/
cp -r ../../bankid-idp/src/main/frontend .

# We make a minor edit to index.html to verify we do not get served the frontend from backend
sed -i '' -e 's/BankID/Externalized-BankID-Frontend/g' frontend/index.html

# 2. Build
#
cd frontend/
rm -rf node_modules
npm install
npm run build
