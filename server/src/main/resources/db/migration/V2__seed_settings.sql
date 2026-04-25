INSERT INTO settings (setting_key, setting_value)
VALUES ('minimum_stock', '5')
ON CONFLICT (setting_key) DO NOTHING;
