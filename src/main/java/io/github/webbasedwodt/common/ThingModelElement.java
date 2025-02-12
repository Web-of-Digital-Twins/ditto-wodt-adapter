package io.github.webbasedwodt.common;

import java.util.Optional;

/**
 * Class representing a Thing Model field.
 */
public class ThingModelElement {
    private final String field;
    private final Optional<String> feature;
    private final Optional<String> domainTag;

    /**
     * Default constructor.
     * @param field the thing model field
     * @param feature the thing feature, if present
     * @param domainTag the domain tag, if present
     */
    public ThingModelElement(String field, Optional<String> feature, Optional<String> domainTag) {
        this.field = field;
        this.feature = (feature.isPresent() && !feature.get().isEmpty()) ? feature : Optional.empty();
        this.domainTag = (domainTag.isPresent() && !domainTag.get().isEmpty()) ? domainTag : Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThingModelElement that = (ThingModelElement) o;
        
        if (!field.equals(that.field)) return false;
        if (!feature.equals(that.feature)) return false;
        return domainTag.equals(that.domainTag);
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + feature.hashCode();
        result = 31 * result + domainTag.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ThingModelElement{" +
                "field='" + field + '\'' +
                ", feature=" + feature +
                ", domainPredicate=" + domainTag +
                '}';
    }

    /**
     * Get the field name.
     * @return the field name
     */
    public String getField() {
        return this.field;
    }

    /**
     * Get the feature, if present.
     * @return an optional for the feature
     */
    public Optional<String> getFeature() {
        return this.feature;
    }

    /**
     * Get the domain tag, if present.
     * @return an optional for the domain tag
     */
    public Optional<String> getDomainTag() {
        return this.domainTag;
    }
}