package com.AMO.autismGame.Login;

import com.AMO.autismGame.Member.Member;
import com.AMO.autismGame.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final MemberRepository memberRepository; // @Autowired 추가

    @PostMapping("/api/auth/identify")
    public Map<String, String> login(@RequestParam("userIdentifier") String userIdentifier,
                                     @RequestParam("phoneNumber") String phoneNumber) {
        Map<String, String> response = new HashMap<>();

        Optional<Member> checkMember = memberRepository.findByUserIdentifier(userIdentifier);

        if (checkMember.isPresent() && checkMember.get().getPhoneNumber().equals(phoneNumber)) {
            response.put("status", "success");
            response.put("message", "User identified successfully");
        } else {
            response.put("status", "error");
            response.put("message", "Invalid user identifier");
        }

        return response;
    }
}
