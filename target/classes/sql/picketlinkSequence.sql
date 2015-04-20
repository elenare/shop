-- Hibernate erwartet ab Version 4.5 die Sequence hibernate_sequence.
-- Bei javax.persistence.schema-generation.create-source ist nur "script" konfiguriert
-- und nicht "metadata-then-script", damit keine Tabellen (z.B. "kunde") der eigentlichen
-- Anwendung erzeugt werden. Dann wird aber auch nicht hibernate_sequence erzeugt.
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
