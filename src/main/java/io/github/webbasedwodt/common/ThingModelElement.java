package io.github.webbasedwodt.common;

import java.util.Optional;

/*
 * Class representing a Thing Model field.
 */
public class ThingModelElement {
    private final String field;
    private final Optional<String> feature;
    private final Optional<String> domainTag;

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

    public String getField() {
        return this.field;
    }

    public Optional<String> getFeature() {
        return this.feature;
    }

    public Optional<String> getDomainTag() {
        return this.domainTag;
    }
}