@echo off
echo ===============================================
echo PayFlow Gateway - Test Data Setup
echo ===============================================

echo.
echo Inserting test data into PayFlow database...

docker exec -i payflow-postgres psql -U payflow_user -d payflowdb -c "
-- Clear existing test data (optional)
DELETE FROM transactions WHERE merchant_id LIKE 'test-%%' OR merchant_id LIKE 'demo-%%' OR merchant_id = 'api-test-merchant';
DELETE FROM merchants WHERE id LIKE 'test-%%' OR id LIKE 'demo-%%' OR id = 'api-test-merchant';

-- Insert test merchants
INSERT INTO merchants (id, name, email, api_key, api_secret, status, business_type, country, is_test_mode, created_at, updated_at, webhook_url, webhook_secret) VALUES
('test-merchant-1', 'Test E-commerce Store', 'ecommerce@test.example', 'pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'ACTIVE', 'E-COMMERCE', 'USA', true, NOW(), NOW(), 'https://webhook.test.example/test-merchant-1', 'webhook_secret_XXX'),
('demo-merchant-123', 'Demo Payment Processor', 'demo@payflow.example', 'pk_demo_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'sk_demo_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'ACTIVE', 'PAYMENT_PROCESSOR', 'USA', true, NOW(), NOW(), 'https://webhook.test.example/demo-merchant-123', 'webhook_secret_XXX'),
('test-merchant-2', 'Sample Online Store', 'store@example.com', 'pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'ACTIVE', 'RETAIL', 'CAN', true, NOW(), NOW(), 'https://webhook.test.example/test-merchant-2', 'webhook_secret_XXX'),
('api-test-merchant', 'API Testing Company', 'api@testcompany.example', 'pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'ACTIVE', 'SOFTWARE', 'GBR', true, NOW(), NOW(), 'https://webhook.test.example/api-test-merchant', 'webhook_secret_XXX');

-- Insert test transactions
INSERT INTO transactions (id, merchant_id, amount, currency, status, description, customer_email, reference_id, created_at, updated_at, authorization_code, processed_at) VALUES
('txn_001_test-merchant-1', 'test-merchant-1', 99.99, 'USD', 'COMPLETED', 'Test transaction for Test E-commerce Store', 'customer1@test.example', 'ref_txn_001_test-merchant-1', NOW() - INTERVAL '1 day', NOW(), 'AUTH_XXXXXXXX', NOW() - INTERVAL '23 hours'),
('txn_002_test-merchant-1', 'test-merchant-1', 149.50, 'USD', 'PENDING', 'Test transaction for Test E-commerce Store', 'customer2@test.example', 'ref_txn_002_test-merchant-1', NOW() - INTERVAL '2 hours', NOW(), NULL, NULL),
('txn_003_test-merchant-1', 'test-merchant-1', 75.25, 'EUR', 'FAILED', 'Test transaction for Test E-commerce Store', 'customer3@test.example', 'ref_txn_003_test-merchant-1', NOW() - INTERVAL '3 days', NOW(), NULL, NULL);

-- Show results
SELECT 'SUCCESS: Test data inserted!' as status;
"

if %errorlevel% equ 0 (
    echo.
    echo ===============================================
    echo Test Data Setup Complete!
    echo ===============================================
    echo.
    echo Available Test API Keys:
    echo.
    echo 1. Test E-commerce Store:
    echo    API Key: pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    echo    Merchant ID: test-merchant-1
    echo.
    echo 2. Demo Payment Processor:
    echo    API Key: pk_demo_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    echo    Merchant ID: demo-merchant-123
    echo.
    echo 3. Sample Online Store:
    echo    API Key: pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    echo    Merchant ID: test-merchant-2
    echo.
    echo 4. API Testing Company:
    echo    API Key: pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    echo    Merchant ID: api-test-merchant
    echo.
    echo ===============================================
    echo Example API Calls:
    echo ===============================================
    echo.
    echo Get merchant info:
    echo curl -H "X-API-Key: pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" http://localhost:8080/api/v1/merchants/test-merchant-1
    echo.
    echo Get transactions:
    echo curl -H "Authorization: Bearer pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" http://localhost:8080/api/v1/payments
    echo.
) else (
    echo ERROR: Failed to insert test data!
    echo Make sure PostgreSQL container is running: docker-compose ps
)

echo.
pause