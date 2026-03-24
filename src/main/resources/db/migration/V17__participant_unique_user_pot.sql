-- Allow 1:N participation (user can join up to 3 pots)
-- Change UNIQUE(user_id) to UNIQUE(user_id, pot_id)
-- Must drop FK first because MySQL won't drop an index used by a FK constraint
ALTER TABLE participants DROP FOREIGN KEY __participants_fk_user_id;
ALTER TABLE participants DROP INDEX __participants_uk;
ALTER TABLE participants ADD UNIQUE KEY __participants_uk_user_pot (user_id, pot_id);
ALTER TABLE participants ADD CONSTRAINT __participants_fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
