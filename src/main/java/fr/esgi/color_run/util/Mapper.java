package fr.esgi.color_run.util;

import fr.esgi.color_run.business.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Mapper {
    public static Course mapRowToCourse(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setId(resultSet.getLong("id"));
        course.setName(resultSet.getString("name"));
        course.setDescription(resultSet.getString("description"));
        course.setAssociationId(resultSet.getInt("associationid"));
        course.setMemberCreatorId(resultSet.getInt("membercreatorid"));
        Timestamp startTs = resultSet.getTimestamp("startdate");
        if (startTs != null) {
            course.setStartDate(startTs.toLocalDateTime());
        }
        Timestamp endTs = resultSet.getTimestamp("enddate");
        if (endTs != null) {
            course.setEndDate(endTs.toLocalDateTime());
        }
        course.setStartpositionLatitude(resultSet.getDouble("startpositionlatitude"));
        course.setStartpositionLongitude(resultSet.getDouble("startpositionlongitude"));
        course.setEndpositionLatitude(resultSet.getDouble("endpositionlatitude"));
        course.setEndpositionLongitude(resultSet.getDouble("endpositionlongitude"));
        course.setDistance(resultSet.getDouble("distance"));
        course.setAddress(resultSet.getString("address"));
        course.setCity(resultSet.getString("city"));
        course.setZipCode(resultSet.getInt("zipcode"));
        course.setMaxOfRunners(resultSet.getInt("maxofrunners"));
        course.setCurrentNumberOfRunners(resultSet.getInt("currentnumberofrunners"));
        course.setPrice(resultSet.getDouble("price"));
        return course;
    }

    public static Member mapRowToMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        m.setFirstname(rs.getString("firstname"));
        m.setEmail(rs.getString("email"));
        m.setPassword(rs.getString("password"));
        m.setPhoneNumber(rs.getString("phoneNumber"));
        m.setAddress(rs.getString("address"));
        m.setCity(rs.getString("city"));
        m.setZipCode(rs.getInt("zipCode"));
        m.setPositionLatitude(rs.getDouble("positionLatitude"));
        m.setPositionLongitude(rs.getDouble("positionLongitude"));

        // CORRECTION IMPORTANTE : Ajouter le mapping du rôle
        try {
            String roleStr = rs.getString("role");
            System.out.println("🔍 Mapper: Role lu depuis BDD pour " + m.getEmail() + ": '" + roleStr + "'");

            if (roleStr != null && !roleStr.trim().isEmpty()) {
                m.setRole(Role.valueOf(roleStr.trim().toUpperCase()));
                System.out.println("✅ Mapper: Role mappé: " + m.getRole());
            } else {
                System.out.println("⚠️ Mapper: Role null/vide, utilisation RUNNER par défaut");
                m.setRole(Role.RUNNER);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Mapper: Role invalide dans la BDD: " + rs.getString("role") + " pour " + m.getEmail());
            System.err.println("❌ Utilisation de RUNNER par défaut");
            m.setRole(Role.RUNNER);
        } catch (SQLException e) {
            System.err.println("❌ Mapper: Erreur lecture colonne role: " + e.getMessage());
            m.setRole(Role.RUNNER);
        }

        return m;
    }

    public static  Association mapRowToAssociation(ResultSet rs) throws  SQLException {
        Association association = new Association();
        association.setId(rs.getLong("id"));
        association.setName(rs.getString("name"));
        association.setDescription(rs.getString("description"));
        association.setWebsiteLink(rs.getString("websiteLink"));
        association.setLogoPath(rs.getString("logoPath"));
        association.setEmail(rs.getString("email"));
        association.setPhoneNumber(rs.getString("phoneNumber"));
        association.setAddress(rs.getString("address"));
        association.setCity(rs.getString("city"));
        int zipCode = rs.getInt("zipCode");
        if (!rs.wasNull()) {
            association.setZipCode(zipCode);
        } else {
            association.setZipCode(null);
        }

        return association;
    }


    public static Course_member mapRowToCourse_member(ResultSet rs) throws SQLException {
        Course_member courseMember = new Course_member();
        courseMember.setId(rs.getLong("id"));
        courseMember.setCourseId(rs.getLong("courseId"));
        courseMember.setMemberId(rs.getLong("memberId"));

        try {
            String regDate = rs.getString("registrationDate");
            courseMember.setRegistrationDate(regDate != null ? regDate : java.time.LocalDateTime.now().toString());

            String status = rs.getString("registrationStatus");
            courseMember.setRegistrationStatus(status != null ? Status.valueOf(status) : Status.ACCEPTED);

            courseMember.setStripeSessionId(rs.getString("stripeSessionId")); // Peut être null

        } catch (SQLException e) {
            courseMember.setRegistrationDate(java.time.LocalDateTime.now().toString());
            courseMember.setRegistrationStatus(Status.ACCEPTED);
            courseMember.setStripeSessionId(null);
        }

        return courseMember;
    }

    public static Message mapRowToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setDiscussionId(rs.getLong("discussionId"));
        message.setMemberId(rs.getLong("memberId"));
        message.setContent(rs.getString("content"));

        // Contenu original
        message.setOriginalContent(rs.getString("originalContent"));

        // Date de création
        Timestamp timestamp = rs.getTimestamp("date");
        if (timestamp != null) {
            message.setDate(timestamp.toLocalDateTime());
        }

        // Date de modification
        Timestamp modifiedTimestamp = rs.getTimestamp("lastModifiedDate");
        if (modifiedTimestamp != null) {
            message.setLastModifiedDate(modifiedTimestamp.toLocalDateTime());
        }

        // Statuts booléens
        message.setPin(rs.getBoolean("isPin"));
        message.setHidden(rs.getBoolean("isHidden"));
        message.setModified(rs.getBoolean("isModified"));
        message.setDeleted(rs.getBoolean("isDeleted"));

        // ID du modérateur qui a masqué (peut être null)
        try {
            Long hiddenBy = rs.getLong("hiddenByMemberId");
            if (!rs.wasNull()) {
                message.setHiddenByMemberId(hiddenBy);
            }
        } catch (SQLException e) {

            message.setHiddenByMemberId(null);
        }

        // Récupération des infos du membre via jointure (si disponibles)
        try {
            message.setMemberFirstname(rs.getString("firstname"));
            message.setMemberName(rs.getString("name"));
        } catch (SQLException e) {
            // Ces colonnes peuvent ne pas être présentes dans toutes les requêtes
        }

        return message;
    }

    // Nouvelle méthode : mapper spécifiquement pour les messages avec jointure membre
//    public static Message mapRowToMessageWithMember(ResultSet rs) throws SQLException {
//        Message message = mapRowToMessage(rs); // Utiliser le mapper de base
//
//        // Forcer la récupération des infos du membre
//        message.setMemberFirstname(rs.getString("firstname"));
//        message.setMemberName(rs.getString("name"));
//
//        return message;
//    }
}