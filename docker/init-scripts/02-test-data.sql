-- Test data for PayFlow Gateway
-- This script creates sample merchants and transactions for testing
-- SECURITY NOTE: Replace API keys and secrets with your actual test values

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
('txn_003_test-merchant-1', 'test-merchant-1', 75.25, 'EUR', 'FAILED', 'Test transaction for Test E-commerce Store', 'customer3@test.example', 'ref_txn_003_test-merchant-1', NOW() - INTERVAL '3 days', NOW(), NULL, NULL),

('txn_001_demo-merchant-123', 'demo-merchant-123', 299.99, 'USD', 'COMPLETED', 'Test transaction for Demo Payment Processor', 'demo1@test.example', 'ref_txn_001_demo-merchant-123', NOW() - INTERVAL '5 hours', NOW(), 'AUTH_XXXXXXXX', NOW() - INTERVAL '4 hours'),
('txn_002_demo-merchant-123', 'demo-merchant-123', 50.00, 'USD', 'COMPLETED', 'Test transaction for Demo Payment Processor', 'demo2@test.example', 'ref_txn_002_demo-merchant-123', NOW() - INTERVAL '1 day', NOW(), 'AUTH_XXXXXXXX', NOW() - INTERVAL '23 hours'),

('txn_001_test-merchant-2', 'test-merchant-2', 199.99, 'CAD', 'COMPLETED', 'Test transaction for Sample Online Store', 'store1@test.example', 'ref_txn_001_test-merchant-2', NOW() - INTERVAL '6 hours', NOW(), 'AUTH_XXXXXXXX', NOW() - INTERVAL '5 hours'),
('txn_002_test-merchant-2', 'test-merchant-2', 89.99, 'CAD', 'PENDING', 'Test transaction for Sample Online Store', 'store2@test.example', 'ref_txn_002_test-merchant-2', NOW() - INTERVAL '30 minutes', NOW(), NULL, NULL),

('txn_001_api-test-merchant', 'api-test-merchant', 500.00, 'GBP', 'COMPLETED', 'Test transaction for API Testing Company', 'api1@testcompany.example', 'ref_txn_001_api-test-merchant', NOW() - INTERVAL '2 days', NOW(), 'AUTH_XXXXXXXX', NOW() - INTERVAL '47 hours'),
('txn_002_api-test-merchant', 'api-test-merchant', 125.75, 'GBP', 'FAILED', 'Test transaction for API Testing Company', 'api2@testcompany.example', 'ref_txn_002_api-test-merchant', NOW() - INTERVAL '1 hour', NOW(), NULL, NULL);

-- Show inserted data
SELECT 'Merchants:' as table_name;
SELECT id, name, email, api_key, status, business_type, country FROM merchants;

SELECT 'Transactions:' as table_name;
SELECT id, merchant_id, amount, currency, status, description FROM transactions LIMIT 10;