ALTER TABLE kunde ADD CONSTRAINT check_geschlecht CHECK (geschlecht IN ('M', 'W'))
ALTER TABLE kunde ADD CONSTRAINT check_familienstand CHECK (familienstand IN ('L', 'VH', 'G', 'VW'))
ALTER TABLE kunde_hobby ADD CONSTRAINT check_hobby CHECK (hobby IN ('S', 'L', 'R'))

ALTER SEQUENCE hibernate_sequence RESTART WITH 5000
