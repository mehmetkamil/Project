package com.yeditepe.firstspingproject.repository;

import com.yeditepe.firstspingproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

  public List<User> findByUsernameStartingWith(String username);
  public Optional<User> findByUsername(String username);
  public User findByUsernameAndEmail(String username, String email);

  //JPQL queries
  @Query("SELECT U FROM User U WHERE U.username=?1 AND U.email=?2")
  public User findUserByUsernameAndEmailUsingJPQL(String username, String email);

  @Query("SELECT U FROM User U WHERE U.username=:username AND U.email=:email")
  public User findUserByUsernameAndEmailUsingJPQLWithParam(@Param("username") String username,
                                                           @Param("email") String email);

  @Query("SELECT COUNT(U) FROM User U WHERE U.username LIKE %:username%")
  public int countUsersWhereUsernameLike(@Param("username") String username);
}
