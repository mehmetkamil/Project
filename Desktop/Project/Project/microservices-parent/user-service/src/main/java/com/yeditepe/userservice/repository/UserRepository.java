package com.yeditepe.userservice.repository;

import com.yeditepe.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    List<User> findByUsernameStartingWith(String username);
    Optional<User> findByUsername(String username);
    User findByUsernameAndEmail(String username, String email);

    @Query("SELECT U FROM User U WHERE U.username=?1 AND U.email=?2")
    User findUserByUsernameAndEmailUsingJPQL(String username, String email);

    @Query("SELECT U FROM User U WHERE U.username=:username AND U.email=:email")
    User findUserByUsernameAndEmailUsingJPQLWithParam(@Param("username") String username,
                                                       @Param("email") String email);

    @Query("SELECT COUNT(U) FROM User U WHERE U.username LIKE %:username%")
    int countUsersWhereUsernameLike(@Param("username") String username);
}
