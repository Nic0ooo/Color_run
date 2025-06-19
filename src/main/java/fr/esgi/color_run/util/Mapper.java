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
        association.setZipCode(rs.getInt("zipCode"));
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
}