package org.ies.fenix.server.repositories;

import org.ies.fenix.server.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {

    List<Game> findAllByOrderByIdDesc();

    @Query("""
    SELECT DISTINCT g
    FROM Game g
    LEFT JOIN FETCH g.dev
    LEFT JOIN FETCH g.tags
    ORDER BY g.id DESC
""")
    List<Game> findAllWithDevAndTagsOrderByIdDesc();

    boolean existsByTitleIgnoreCase(String title);

    List<Game> findByTitleContainingIgnoreCase(String title);

    List<Game> findByDevId(Integer devId);

    List<Game> findByTagsId(Integer tagId);

    List<Game> findByDev_Username(String username);

    long countByDevId(Integer devId);

    @Query("""
        SELECT g
        FROM Game g
        JOIN g.tags t
        WHERE t.name IN :names
        GROUP BY g
        HAVING COUNT(DISTINCT t.name) = :size
    """)
    List<Game> findByAllTagNames(@Param("names") List<String> names,
                                 @Param("size") long size);

    @Query("""
    SELECT DISTINCT g
    FROM Game g
    LEFT JOIN FETCH g.dev
    LEFT JOIN FETCH g.tags
    WHERE g.id = :id
""")
    Optional<Game> findByIdWithDevAndTags(@Param("id") Integer id);
}