package net.unicon.toro.installer.tools;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import net.unicon.academus.domain.lms.Role;

import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.GroupService;

public final class CmsGroupInitialization {
    
    private CmsGroupInitialization() {
        
    }
    
    private static IEntityGroup getOrCreateGroup(IEntityGroup parent, String name) {
        ILockableEntityGroup lockableGroup;
        IEntityGroup group = parent.getMemberGroupNamed(name);
        if (group == null) {
            System.out.println("Locking group: " + parent.getKey());
            lockableGroup = GroupService.findLockableGroup(parent.getKey(), CmsGroupInitialization.class.getName());
            group = GroupService.newGroup(parent.getLeafType());
            group.setCreatorID("admin");
            group.setName(name);
            group.update();
            lockableGroup.addMember(group);
            lockableGroup.update();
            System.out.println("Created group " + name + "("+group.getKey()+").");
        } else {
            System.out.println("Found group " + name + "("+group.getKey()+").");
        }
        return group;
    }
    
    private static void ensureRoleExists(int roleId, String label, int type, int groupId) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        try {
            conn = RDBMServices.getConnection();
            String sql = "select role_id from role where role_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, roleId);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Found role " + label + ".");
            } else {
                sql = "insert into role (role_id, offering_id, label, type, group_id) values (?,?,?,?,?)";
                ps2 = conn.prepareStatement(sql);
                ps2.setInt(1, roleId);
                ps2.setInt(2, -1);
                ps2.setString(3, label);
                ps2.setInt(4, type);
                ps2.setInt(5, groupId);
                ps2.execute();
                System.out.println("Created role " + label + ".");
            }
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
            if (ps2 != null) {
                ps2.close();
                ps2 = null;
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            IEntityGroup everyone = GroupService.getDistinguishedGroup(GroupService.EVERYONE);
            if (everyone == null) throw new RuntimeException("Failed to find Everyone group!");
            System.out.println("Found Everyone group.");
            IEntityGroup cms = getOrCreateGroup(everyone, "CMS");
            IEntityGroup roles = getOrCreateGroup(cms, "Roles");
            IEntityGroup system = getOrCreateGroup(roles, "System");
            IEntityGroup offering =  getOrCreateGroup(roles, "Offering");
            
            Properties p = new Properties();
            // system roles
            String[] systemRoles = { "Administrator", "Facilitator", "User", "Guest" };
            int i;
            for (i=0; i<systemRoles.length; i++) {
                IEntityGroup roleGroup = getOrCreateGroup(system, systemRoles[i]);
                ensureRoleExists(i+1, systemRoles[i], Role.SYSTEM, new Integer(roleGroup.getLocalKey()).intValue());
                p.setProperty(systemRoles[i]+"Key", roleGroup.getKey());
            }
            
            String[] offeringRoles = { "Sponsor", "Assistant", "Member", "Observer" };
            for (int j=0; j<offeringRoles.length; j++) {
                IEntityGroup roleGroup = getOrCreateGroup(offering, offeringRoles[j]);
                ensureRoleExists(j+1+i, offeringRoles[j], Role.OFFERING, new Integer(roleGroup.getLocalKey()).intValue());
                p.setProperty(offeringRoles[j]+"Key", roleGroup.getKey());
            }
            
            FileOutputStream os = new FileOutputStream("roleGroupKeys.properties");
            p.store(os,"");
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
