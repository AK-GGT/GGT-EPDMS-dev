package de.iai.ilcd.model.security;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Class that represents user group entity
 */
@Entity
@Table(name = "usergroup")
public class UserGroup implements ISecurityEntity, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    // Does not contain CascadeType.REMOVE
    private List<User> users = new ArrayList<User>();

    /**
     * The organization of the group
     */
    @ManyToOne
    @JoinColumn(name = "organization")
    private Organization organization;

    /**
     * Create a new instance of UserGroup
     */
    public UserGroup() {

    }

    /**
     * Create a new instance of UserGroup and set the group name
     *
     * @param groupName the group name to set
     */
    public UserGroup(String groupName) {
        this.setGroupName(groupName);
    }

    /**
     * Get the id
     *
     * @return the id
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Set the id
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the group name
     *
     * @return the group name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Set the group name
     *
     * @param groupName the group name to set
     */
    public void setGroupName(String groupName) {
        if (groupName != null) {
            this.groupName = groupName.trim();
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        return this.groupName;
    }

    /**
     * Get the list of users
     *
     * @return the list of users
     */
    public List<? extends IUser> getUsers() {
        return this.users;
    }

    /**
     * Set the list of users
     *
     * @param users the list of users to set
     */
    protected void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Sorts the cached list of assigned users by name and returns it afterwards.
     *
     * @return sorted list of associated users
     */
    public List<User> getUsersSorted() {
        Collections.sort(this.users,
                (u1, u2) -> u1.getUserName().toLowerCase(Locale.ROOT)
                        .compareTo(u2.getUserName().toLowerCase(Locale.ROOT))); // Anonymous Comparator<User>

        return this.users;
    }

    /**
     * Add the user to this group (this includes also {@linkplain User#addToGroup(UserGroup) adding} this group to the
     * respective user's groups list)
     *
     * @param user the user to add to this group
     */
    public void addUser(User user) {
        if (!this.containsUser(user)) {
            this.users.add(user);
            user.addToGroup(this);
        }
    }

    /**
     * Remove the user from this group (this includes also {@linkplain User#removeFromGroup(UserGroup) removing} this
     * group from the respective user's groups list)
     *
     * @param user the user to remove from this group
     */
    public void removeUser(IUser user) {
        if (this.containsUser(user)) {
            this.users.remove(user);
            user.removeFromGroup(this);
        }
    }

    /**
     * Remove all users from this group (this includes also {@linkplain User#removeFromGroup(UserGroup) removing} this
     * group from the respective user's group list)
     */
    public void removeAllUsers() {
        for (IUser userToRemove : this.users) {
            userToRemove.removeFromGroup(this);
        }
        this.users = null;
    }

    /**
     * Get the organization of this group
     *
     * @return organization of this group
     */
    public Organization getOrganization() {
        return this.organization;
    }

    /**
     * Set the organization of this group
     *
     * @param organization organization of this group to set
     */
    public void setOrganization(Organization organization) {
        if (ObjectUtils.equals(this.organization, organization)) {
            return;
        }
        Organization oldOrg = this.organization;

        if (oldOrg != null) {
            oldOrg.removeGroup(this);
        }

        this.organization = organization;
        if (this.organization != null) {
            this.organization.addGroup(this);
        }
    }

    /**
     * Convenience method to determine if group has users
     *
     * @return <code>true</code> if group has users, <code>false</code> otherwise
     */
    public boolean hasUsers() {
        return CollectionUtils.isNotEmpty(this.users);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.groupName == null) ? 0 : this.groupName.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserGroup)) {
            return false;
        }
        UserGroup other = (UserGroup) obj;
        if (this.groupName == null) {
            if (other.groupName != null) {
                return false;
            }
        } else if (!this.groupName.equals(other.groupName)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "de.iai.ilcd.model.security.Group=[groupName:" + this.groupName + "]";
    }

    /**
     * Determine if given user is in this group
     *
     * @param user user to check
     * @return <code>true</code> if user is in group, <code>false</code> otherwise
     */
    public boolean containsUser(IUser user) {
        return this.users != null && this.users.contains(user);
    }

}
