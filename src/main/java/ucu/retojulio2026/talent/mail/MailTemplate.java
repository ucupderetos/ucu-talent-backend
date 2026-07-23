package ucu.retojulio2026.talent.mail;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MailTemplate {

    private String studentName;
    private String companyName;
    private boolean selected;
    private String vacanyName;


    public void applicationRecieved(String studentName, String companyName, String vacanyName) {

    }
}
