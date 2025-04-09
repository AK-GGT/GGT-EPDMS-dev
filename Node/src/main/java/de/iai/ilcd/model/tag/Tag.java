package de.iai.ilcd.model.tag;

import de.iai.ilcd.model.datastock.AbstractDataStock;
import de.iai.ilcd.model.process.Process;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Entity
@Table(name = "tag")
public class Tag implements Serializable {

    /**
     * Serialization ID
     */
    private static final long serialVersionUID = -8653425645726217710L;
    @ManyToMany(mappedBy = "tags")
    protected Set<Process> processes = new HashSet<Process>();
    @ManyToMany(mappedBy = "tags")
    protected Set<AbstractDataStock> ds = new HashSet<AbstractDataStock>();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    private boolean visible = false;

    public Tag() {
        super();
    }

    public Tag(String name) {
        if (name == null)
            throw new NullPointerException("Provided tag name was null.");
        this.name = name;
    }

    /**
     * @return the ID of Tag
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the ID of Tag to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the processes
     */
    public Set<Process> getProcesses() {
        return processes;
    }

    /**
     * @param processes the processes to set
     */
    public void setProcesses(Set<Process> processes) {
        this.processes = processes;
    }

    /**
     * @return the ds
     */
    public Set<AbstractDataStock> getDs() {
        return ds;
    }

    /**
     * @param ds the ds to set
     */
    public void setDs(Set<AbstractDataStock> ds) {
        this.ds = ds;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean containedInList(List<Tag> tagList) {
        for (Tag t : tagList) {
            if (name.equals(t.getName()))
                return true;
        }
        return false;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean value) {
        this.visible = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tag tag = (Tag) o;
//		if (tag.name == null || name == null)
        return Objects.equals(name, tag.name);
//
//		return Objects.equals(name.toLowerCase(Locale.ROOT), tag.name.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
//		if (name == null)
        return Objects.hash(null);
//		return Objects.hash(name.toLowerCase(Locale.ROOT));
    }
}
