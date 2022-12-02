package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional // Default: 테스트에서는 기본적으로 롤백함
class MemberServiceTest {

    // 테스트 코드에서는 한번 주입된 게 바뀔 일이 거의 없으므로 필드 인젝션도 괜찮음.
    @Autowired
    MemberService memberService; 
    @Autowired
    MemberRepository memberRepository;
    
    @Test
    @DisplayName("회원가입")
    public void 회원가입() throws Exception {
        // given
        Member member = new Member();
        member.setName("Kim");
        
        // when
        Long savedId = memberService.join(member);
        
        // then
        assertEquals(member, memberRepository.findOne(savedId));
    }

    @Test
    @DisplayName("중복회원예외")
    public void 중복회원예외() throws Exception {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);

        // then
        assertThrows(IllegalStateException.class, () -> {
            memberService.join(member2);
        });
    }

}