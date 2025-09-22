# PayFlow Gateway - Security Setup Guide

🚨 **CRITICAL: Follow these security steps before pushing to GitHub or deploying to production!**

## Security Configuration Required

This project contains template files that need to be configured with your actual secrets. **Never commit real secrets to version control.**

### 1. Environment Variables Setup

1. **Copy the environment template:**
   ```bash
   copy .env.example .env
   ```

2. **Edit `.env` with your actual values:**
   - Replace all `your_*_password_here` with strong, unique passwords
   - Replace `your_very_long_jwt_secret_key_at_least_512_bits_here` with a secure JWT secret
   - Use a password generator for all secrets

### 2. Application Properties Setup

1. **Copy the application properties template:**
   ```bash
   copy src\main\resources\application.properties.template src\main\resources\application.properties
   ```

2. **Set environment variables** or update the properties file with your actual database and security configuration.

### 3. Test Data Setup

The files `setup-test-data.bat` and `docker/init-scripts/02-test-data.sql` contain placeholder API keys marked with `XXXXXXXX`. 

**Before running tests:**
1. Replace placeholder API keys with your actual test API keys
2. Replace placeholder secrets with your actual webhook secrets
3. Update email domains from `.example` to your actual test domains

### 4. Git Security

The following files are automatically ignored by `.gitignore`:
- `.env` (environment variables)
- `src/main/resources/application.properties` (configuration with secrets)
- `*.log` (logs that might contain sensitive data)

### 5. Production Deployment

**Never use the example/template values in production!**

- Generate strong, unique passwords for all services
- Use proper JWT secrets (at least 512 bits)
- Enable proper SSL/TLS certificates
- Set up proper webhook URLs
- Use production-grade API keys

### 6. Security Checklist

Before deploying or pushing to GitHub:

- [ ] All secrets are in environment variables or secure configuration
- [ ] No hardcoded passwords or API keys in code
- [ ] `.env` file is not committed to git
- [ ] `application.properties` with real secrets is not committed
- [ ] Test data uses placeholder or anonymized values
- [ ] Production configurations are separate from development
- [ ] All webhook URLs point to legitimate endpoints
- [ ] JWT secrets are properly generated and secure

### 7. Emergency Response

If you accidentally commit secrets:
1. **Immediately rotate all exposed credentials**
2. **Force push to remove secrets from git history:**
   ```bash
   git filter-branch --force --index-filter 'git rm --cached --ignore-unmatch src/main/resources/application.properties' --prune-empty --tag-name-filter cat -- --all
   git push origin --force --all
   ```
3. **Update all affected systems with new credentials**

## Support

For security-related questions, please:
1. Review this security guide thoroughly
2. Check the application logs for configuration errors
3. Ensure all environment variables are properly set

Remember: **Security is not optional for a payment gateway!**