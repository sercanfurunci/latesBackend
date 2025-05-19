-- Mevcut null veya false olan is_active değerlerini true yap
UPDATE users SET is_active = true WHERE is_active IS NULL OR is_active = false;

-- Tüm kullanıcıların is_active değerlerini kontrol et
SELECT id, email, is_active FROM users; 