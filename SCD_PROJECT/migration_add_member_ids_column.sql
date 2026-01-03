-- Migration Script: Add member_ids column to fyp_groups table
-- Execute this script manually if Hibernate auto-update doesn't work

-- Add member_ids column as JSON type
ALTER TABLE fyp_groups 
ADD COLUMN member_ids JSON DEFAULT '[]' AFTER created_at;

-- Update existing groups with their member IDs
UPDATE fyp_groups g
SET member_ids = (
    SELECT CONCAT('[', GROUP_CONCAT(u.id ORDER BY u.id), ']')
    FROM users u
    WHERE u.group_id = g.id
    AND u.role_id = (SELECT id FROM roles WHERE name = 'STUDENT')
)
WHERE EXISTS (
    SELECT 1 FROM users u 
    WHERE u.group_id = g.id 
    AND u.role_id = (SELECT id FROM roles WHERE name = 'STUDENT')
);

-- Set empty array for groups with no members
UPDATE fyp_groups 
SET member_ids = '[]' 
WHERE member_ids IS NULL;

