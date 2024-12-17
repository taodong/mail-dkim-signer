package io.github.taodong.mail.dkim.model;


import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public record DkimSignHeader(@NotBlank String name, boolean required) {

    public DkimSignHeader(@NotBlank String name) {
        this(name, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DkimSignHeader that = (DkimSignHeader) o;

        return new EqualsBuilder().append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).toHashCode();
    }
}
