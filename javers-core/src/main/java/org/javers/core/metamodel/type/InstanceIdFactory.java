package org.javers.core.metamodel.type;

import org.javers.common.exception.JaversException;
import org.javers.common.exception.JaversExceptionCode;
import org.javers.common.validation.Validate;
import org.javers.core.metamodel.object.InstanceId;

import java.lang.reflect.Type;

class InstanceIdFactory {
    private final EntityType entityType;

    InstanceIdFactory(EntityType entityType) {
        this.entityType = entityType;
    }

    InstanceId create(Object localId) {
        Validate.argumentsAreNotNull(entityType, localId);

        Object dehydratedLocalId = dehydratedLocalId(entityType, localId);

        String localIdAsString = localIdAsString(dehydratedLocalId);

        return new InstanceId(entityType.getName(), dehydratedLocalId, localIdAsString);
    }

    InstanceId createFromDehydratedLocalId(Object dehydratedLocalId) {
        Validate.argumentsAreNotNull(entityType, dehydratedLocalId);

        String localIdAsString = localIdAsString(dehydratedLocalId);

        return new InstanceId(entityType.getName(), dehydratedLocalId, localIdAsString);
    }

    String localIdAsString(Object dehydratedLocalId) {
        if (isIdEntity()) {
            EntityType idPropertyType = entityType.getIdPropertyType();
            return idPropertyType.getInstanceIdFactory().localIdAsString(dehydratedLocalId);
        }
        if (isIdValueObject()) {
            return dehydratedLocalId.toString();
        }
        if (isIdPrimitiveOrValue()) {
            PrimitiveOrValueType primitiveOrValueType = entityType.getIdProperty().getType();
            return primitiveOrValueType.smartToString(dehydratedLocalId);
        }

        throw idTypeNotSupported();
    }

    private JaversException idTypeNotSupported() {
        return new JaversException(JaversExceptionCode.ID_TYPE_NOT_SUPPORTED,
                entityType.getIdProperty().getType().getName(),
                entityType.getBaseJavaClass().getName());
    }

    Type getLocalIdDehydratedType() {
        if (isIdEntity()) {
            EntityType idPropertyType = entityType.getIdPropertyType();
            return idPropertyType.getIdPropertyGenericType();
        }
        if (isIdValueObject()) {
            return String.class;
        }
        if (isIdPrimitiveOrValue()) {
            return entityType.getIdPropertyGenericType();
        }

        throw idTypeNotSupported();
    }

    private Object dehydratedLocalId(EntityType entityType, Object localId) {
        if (isIdEntity()) {
            EntityType idPropertyType = entityType.getIdPropertyType();
            return idPropertyType.getIdOf(localId);
        }
        if (isIdValueObject()) {
            ValueObjectType valueObjectType = entityType.getIdPropertyType();
            return valueObjectType.smartToString(localId);
        }
        if (isIdPrimitiveOrValue()) {
            return localId;
        }

        throw idTypeNotSupported();
    }

    private boolean isIdEntity() {
        return entityType.getIdProperty().getType() instanceof EntityType;
    }

    private boolean isIdValueObject() {
        return entityType.getIdProperty().getType() instanceof ValueObjectType;
    }

    private boolean isIdPrimitiveOrValue() {
        return entityType.getIdProperty().getType() instanceof PrimitiveOrValueType;
    }
}