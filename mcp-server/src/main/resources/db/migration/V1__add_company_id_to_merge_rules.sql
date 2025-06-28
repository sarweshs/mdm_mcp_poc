-- Migration: Add company_id column to merge_rules table
-- This allows rules to be either global (company_id = null) or company-specific

ALTER TABLE merge_rules ADD COLUMN company_id VARCHAR(255);

-- Add comment to explain the column purpose
COMMENT ON COLUMN merge_rules.company_id IS 'Company ID for company-specific rules. NULL means global rule available to all companies.';

-- Create index for better query performance when filtering by company_id
CREATE INDEX idx_merge_rules_company_id ON merge_rules(company_id);

-- Create index for queries that need both company_id and is_active
CREATE INDEX idx_merge_rules_company_active ON merge_rules(company_id, is_active); 