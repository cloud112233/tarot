package com.myee.tarot.apiold.domain;

import com.myee.tarot.core.GenericEntity;

import javax.persistence.*;

/**
 * Created by chay on 2016/8/19.
 */
@Entity
@javax.persistence.Table(name = "CA_MATERIAL_FILE_KIND")
public class MaterialFileKind extends GenericEntity<Long, MaterialFileKind> {
    @Id
    @Column(name = "MATERIAL_FILE_KIND_ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ACTIVE", columnDefinition = "INT",length = 1)
    private int active;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}