package com.bobo.boboagent.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.sql.*;

public class FamousQuotesUtil {

    private static String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/bobo_agent?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";

    private static String userName = "root";

    private static String password = "991204";


    public static String getFamousQuotes(String name) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, userName, password)) {
            String sql = "SELECT * FROM famous_quotes WHERE author LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "%" + name + "%");
            
            ResultSet resultSet = preparedStatement.executeQuery();
            JSONArray jsonArray = new JSONArray();
            
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.set("author", resultSet.getString("author"));
                jsonObject.set("quote", resultSet.getString("quote"));
                jsonArray.add(jsonObject);
            }
            
            if (jsonArray.isEmpty()) {
                JSONObject errorJson = new JSONObject();
                errorJson.set("error", "没有收录该人名人名言");
                return errorJson.toString();
            }
            
            return jsonArray.toString();
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
