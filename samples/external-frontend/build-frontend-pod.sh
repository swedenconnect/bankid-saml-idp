# This script builds an nginx pod with static content of our frontend

# 1. Fetch Sources

# We choose to copy since we are in the same git repository
# We recommend you  to do a spare-checkout on tagged releases
mkdir frontend/
cp -r ../../bankid-idp/bankid-idp-frontend/ frontend/

# 2. Build
cd frontend/
rm -rf node_modules
npm install
npm run build
