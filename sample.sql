

delete from admins;
delete from students;
delete from programs;
delete from applications;

INSERT INTO admins (username, password, email, display_name)
VALUES
    ('admin', '$2a$12$5IsO9NAz7Qw3HS8HpLH2zupiwFMOghToiEWaEjhBdTmG2r7UCrKyK', 'admin@example.com', 'Administrator Surname');

-- Populate students table
INSERT INTO students (username, password, email, display_name)
VALUES
    ('delali', '$2a$10$EJKVcHN7yA.CvCJktVuDkuJ6in5rLgfu32hmaWVIoGAMf8aQGAstG', 'delali@example.com', 'Delali Cudjoe'),
    ('delali', '$2a$10$EJKVcHN7yA.CvCJktVuDkuJ6in5rLgfu32hmaWVIoGAMf8aQGAstG', 'delali@example.com', 'Delali Cudjoe'),
    ('student2', 'password101', 'student2@example.com', 'Student Two');
ALTER TABLE programs
ALTER COLUMN description TYPE VARCHAR(10000);
-- Populate programs table
INSERT INTO programs (id, title, year, semester, application_open, application_close, start_date, end_date, faculty_lead, description)
VALUES
    (1, 'Software Engineering Abroad', 2025, 'FALL', '2025-01-01T08:00:00Z', '2025-04-30T23:59:59Z', '2025-09-01', '2025-12-15', 'Dr. Smith',
     'This program offers students a unique opportunity to study software engineering in an international setting. Participants will explore software development methodologies, industry best practices, and emerging technologies while gaining exposure to global perspectives on software engineering.

     Students will collaborate with local professionals and universities, engage in hands-on projects, and participate in technical workshops. The experience will enhance their problem-solving skills, cultural adaptability, and technical expertise, preparing them for a career in an increasingly interconnected world.'),

(2, 'Cultural Studies in Europe', 2025, 'SPRING', '2024-08-01T08:00:00Z', '2025-11-30T23:59:59Z', '2025-01-15', '2025-05-15', 'Dr. Johnson',
    'This immersive program takes students on a journey through the rich and diverse cultures of Europe. Participants will visit historic landmarks, engage with local communities, and study the social, artistic, and historical influences that have shaped European civilizations.

    Through guided tours, guest lectures, and hands-on cultural activities, students will gain a deeper understanding of European traditions, languages, and contemporary societal dynamics. The program encourages critical thinking, cross-cultural appreciation, and academic growth in the field of cultural studies.');

-- Populate applications table
INSERT INTO applications (id, student, program_id, date_of_birth, gpa, major, answer1, answer2, answer3, answer4, answer5, status)
VALUES
    ('1-delali', 'delali', 1, '2000-05-14', 3.8, 'Computer Science', 'Participating in this study abroad program represents an invaluable opportunity for me to immerse myself in a culture distinct from my own, allowing me to challenge my assumptions and broaden my worldview. Growing up in a multicultural society, I have always been fascinated by the interplay between culture and identity. This program offers me a platform to deepen that understanding by engaging firsthand with local communities, traditions, and perspectives. Additionally, I am eager to explore the academic offerings of the host institution, which provides courses and experiential learning that are not available at my home university. These courses will equip me with a nuanced understanding of global issues, particularly those related to my field of study, which is international relations. By participating in this program, I aim to develop not only as a student but also as a global citizen, gaining the cultural sensitivity and adaptability essential in today’s interconnected world.', 'This study abroad program aligns perfectly with my academic and career aspirations, as I am deeply committed to pursuing a career in international development. The curriculum’s focus on sustainable development and cross-cultural communication directly complements my major in political science and my minor in economics. By engaging with experts and local leaders who tackle real-world problems, I will gain practical insights into the complexities of implementing policy across different cultural contexts. Additionally, the opportunity to intern with a local NGO will provide hands-on experience that is vital for my career path. Exposure to different governance models and economic strategies will deepen my understanding of how policy impacts various communities differently. Ultimately, this experience will empower me with the skills and perspectives necessary to advocate for inclusive and sustainable development solutions on a global scale.', 'One of the challenges I anticipate is the language barrier. Although I have taken introductory courses in the local language, I am not yet fluent, which might limit my ability to fully engage with locals and understand cultural nuances. To address this, I plan to immerse myself in the language before and during the program by practicing with native speakers, utilizing language-learning apps, and participating in language exchange groups. Another potential challenge is adapting to different academic expectations and teaching styles. I expect coursework to be more discussion-based and less structured than I am used to. To overcome this, I will actively seek feedback from professors and peers and make use of academic support services if needed. Embracing a mindset of openness and flexibility will be crucial, allowing me to view challenges as opportunities for growth and learning.', 'When I moved from my hometown to attend university, I encountered an environment vastly different from what I was accustomed to. Coming from a small town where everyone knew each other, the fast-paced city life and diverse student body were overwhelming at first. To adapt, I made a concerted effort to embrace this diversity by joining cultural clubs and participating in events that celebrated different heritages. I also developed new study habits to keep up with the rigorous academic demands. Over time, I grew to appreciate the vibrancy and opportunities the city offered. I learned how to navigate public transportation, tried foods from around the world, and forged friendships across cultural divides. This experience taught me resilience and the importance of seeking common ground amidst differences, skills I am confident will serve me well in a study abroad setting.', 'Having grown up in a multicultural family, I bring a unique perspective that values and understands the complexities of cultural identity and communication. I am adept at bridging cultural gaps and fostering inclusive dialogues, skills I have honed through leadership roles in various student organizations. Additionally, my academic background in conflict resolution equips me with tools to mediate discussions and encourage collaborative problem-solving. I am also passionate about storytelling and have experience using media to highlight underrepresented voices, a skill I hope to use to document and share our group’s experiences. By contributing my cultural sensitivity, leadership, and media skills, I aspire to help create a supportive and enriching environment for all participants, fostering a community where everyone feels heard and valued.', 'APPLIED'),
('1-student2', 'student2', 1, '2001-09-21', 3.5, 'History', 'Studying abroad has been a long-standing goal of mine because I believe that true learning happens beyond the classroom. This program offers the chance to step outside my comfort zone and experience a new way of life firsthand. As someone passionate about global issues, I want to gain a deeper appreciation for different cultural perspectives and develop the adaptability necessary for working in international settings. Additionally, I see this as an opportunity to challenge myself academically and personally, by engaging in courses taught in a different educational system and interacting with peers from diverse backgrounds. Beyond academics, I am also eager to immerse myself in the daily life of the host country—whether that’s through local traditions, food, or historical sites. This experience will not only shape my academic journey but also broaden my personal outlook on the world.', 'As a business major with an interest in global markets, this study abroad program is an ideal opportunity to gain international experience and firsthand insight into how businesses operate in different cultural contexts. In today’s interconnected world, understanding the nuances of foreign markets, consumer behavior, and economic policies is critical. The courses offered in this program, particularly those on international trade and cross-cultural management, align perfectly with my academic focus. Additionally, I hope to build relationships with industry professionals through networking events and site visits, which could provide future internship or job opportunities. Long-term, I aspire to work in international business development, and this program will give me the cultural competence and real-world exposure necessary to excel in that field.', 'One of the biggest challenges I anticipate is homesickness. While I have lived away from home for college, studying abroad means being in an entirely new environment with a different culture, language, and social norms. To address this, I plan to stay connected with my friends and family through regular calls while also focusing on forming new connections in my host country. Joining student organizations and engaging in cultural activities will help me build a support network. Another challenge I expect is navigating daily life in a new language. While I have studied the local language, I know there will be moments of miscommunication. I plan to approach these situations with patience and a sense of humor, using translation apps when necessary and actively improving my language skills through practice with locals.', 'During my first internship, I was placed in a fast-paced corporate setting that was completely unfamiliar to me. The office culture was very different from what I was used to—expectations were high, and I had to learn quickly to keep up with my colleagues. At first, I struggled with the unspoken norms and professional jargon. Instead of becoming discouraged, I took the initiative to ask questions, observe how my coworkers handled tasks, and seek feedback from my supervisor. Over time, I adjusted and became more confident in my role, even taking on additional responsibilities. This experience taught me the importance of adaptability and the value of stepping into unfamiliar territory. I believe these skills will be crucial during my study abroad experience, where I will once again need to embrace change and navigate a new cultural landscape.', 'I bring a strong sense of curiosity and a willingness to learn from others. One of my greatest strengths is my ability to listen and engage in meaningful conversations with people from different backgrounds. I believe that everyone has a story to share, and I enjoy hearing different viewpoints and learning from diverse experiences. Additionally, I have a deep interest in history and enjoy exploring how historical events shape modern societies, which I think will enrich our group discussions. My problem-solving skills and ability to stay calm under pressure also make me a valuable team member—I can help mediate conflicts or offer creative solutions when challenges arise. I hope to contribute to this program by fostering a supportive and open-minded group dynamic, ensuring that we all learn from one another and make the most of this experience.', 'ENROLLED');
