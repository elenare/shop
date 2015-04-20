-- Tabellen manuell entleeren, weil das Neuanlegen durch das WildFly-Subsystem "picketlink" bereits erfolgt ist
-- SELECT *
-- FROM   information_schema.constraints
-- WHERE  constraint_type = 'REFERENTIAL'

DELETE FROM RelationshipIdentityTypeEntity
DELETE FROM RelationshipTypeEntity
DELETE FROM AccountTypeEntity
DELETE FROM AttributeTypeEntity
DELETE FROM GroupTypeEntity
DELETE FROM RoleTypeEntity
DELETE FROM PasswordCredentialTypeEntity

DELETE FROM IdentityTypeEntity
DELETE FROM PartitionTypeEntity
DELETE FROM AttributedTypeEntity
