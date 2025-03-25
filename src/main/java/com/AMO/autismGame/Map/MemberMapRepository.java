package com.AMO.autismGame.Map;

import com.AMO.autismGame.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberMapRepository extends JpaRepository<MemberMap, Integer> {
    List<MemberMap> findByMember(Member member);
    boolean existsByMember(Member member);
    Optional<MemberMap> findByMemberAndMap_MapID(Member member, String s);

}
