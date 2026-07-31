-- Event data compiled from Event.docx and University_Club_Events (1).docx.
-- `image_url` is intentionally NULL until cover images are available.
INSERT INTO events
    (title, category, date, location, description, image_url, university, event_type, status, about)
VALUES
    ('DL Sprint 4.0 - BUET CSE Fest 2026', 'Competition', '22 Days', 'Kaggle (Online)',
     'A 22-day machine learning and deep learning competition featuring two Kaggle challenges.',
     NULL, 'Bangladesh University of Engineering and Technology (BUET)', 'Competition', 'PUBLISHED',
     'Organizer: BUET Computer Club (BCC). As part of BUET CSE Fest 2026, participants can apply data science, machine learning, and problem-solving skills in two Kaggle competitions. Competition links and the rulebook are shared through registered email and the official event post; participants should submit before the deadline and can ask questions through email or the Kaggle discussion forum.'),

    ('BUET CSE Fest 2026 - Hackathon', 'Competition', 'Preliminary: 22 January 2026; Final: 29 January 2026', 'BUET, Dhaka',
     'A nationwide hackathon with an online preliminary round and onsite final for shortlisted university teams.',
     NULL, 'Bangladesh University of Engineering and Technology (BUET)', 'Hackathon', 'PUBLISHED',
     'Organizer: IEEE Computer Society BUET Student Branch Chapter. Teams develop innovative solutions to real-world challenges through an online preliminary round, followed by an onsite final at BUET. Registration deadline: 21 January 2026, 11:00 PM. Preliminary round: 22 January 2026. Final round: 29 January 2026. Preliminary participation is free; selected finalists pay BDT 1,000. Contact: hackathon.buetcsefest2026@gmail.com.'),

    ('Agentic AI Hackathon', 'Competition', 'Preliminary: 9 July 2026; Final: 24-25 July 2026', 'AB2 301, 302 & Auditorium, Islamic University of Technology (IUT), Gazipur, Bangladesh',
     'A two-stage AI hackathon where teams build and present autonomous AI applications.',
     NULL, 'Islamic University of Technology (IUT)', 'Hackathon', 'PUBLISHED',
     'Organizer: IUT Computer Society (IUTCS), as part of IUT 12th ICT Fest 2026. The online preliminary round runs 9 July 2026, 6:00 PM-10:00 PM. Selected teams advance to a 24-hour onsite final from 24 July 2026, 10:00 AM to 25 July 2026, 10:00 AM. Team size: 1-3 members. Preliminary round is free; the final costs BDT 2,000 per team. Activities include agentic AI development, project presentations before judges, and networking.'),

    ('Watt A Pitch', 'Competition', 'Registration deadline: 14 August 2025', 'Islamic University of Technology (IUT), Gazipur, Bangladesh',
     'An engineering idea-pitching competition for teams presenting practical solutions to real-world challenges.',
     NULL, 'Islamic University of Technology (IUT)', 'Competition', 'PUBLISHED',
     'Organizer: IEEE IUT Student Branch. Teams of 3-4 present innovative engineering ideas before a judging panel. Registration costs BDT 120 for a team of 3 or BDT 160 for a team of 4. The event focuses on innovation, problem-solving, presentation skills, teamwork, and networking.'),

    ('IUT Interclub 2025', 'Culture', '1-3 August 2025', 'Islamic University of Technology (IUT), Gazipur, Bangladesh',
     'A three-day institutional British Parliamentary debating tournament for school, college, and university students.',
     NULL, 'Islamic University of Technology (IUT)', 'Debate Tournament', 'PUBLISHED',
     'Organizer: IUT Debating Society (IUTDS). One of Bangladesh''s largest institutional British Parliamentary debating tournaments, featuring multiple debate rounds for students from schools, colleges, and universities. Registration fee: BDT 3,670 per team. The event promotes critical thinking, public speaking, analytical reasoning, teamwork, and networking.'),

    ('Online Webinar - কলকাতার কালচারাল হেজেমনি ও মুসলিম হিসেবে আমাদের করণীয়', 'Workshop', '8 May 2026, 9:00 PM (tentative)', 'Google Meet (Live)',
     'An online Islamic educational webinar on cultural hegemony, Muslim identity, and contemporary responsibilities.',
     NULL, 'Jahangirnagar University (JU)', 'Webinar', 'PUBLISHED',
     'Organizer: JU Society of Islamic Knowledge Seekers (JUSIKS). Keynote speaker: Hafiz Jakaria Masud. The live Google Meet session discusses cultural influence, Islamic values, Muslim identity, and practical guidance for Muslim youth, with an interactive Q&A.'),

    ('YUNet International Youth Upskill Summit 2026', 'Workshop', '17 July 2026', 'Virtual',
     'A global virtual summit on future-ready skills, careers, higher education, entrepreneurship, and freelancing.',
     NULL, 'Jahangirnagar University (JU)', 'Summit', 'PUBLISHED',
     'Organizer: Youth Upskill Network (YUNet). Collaborating club: Jahangirnagar University Career Club (JUCC). Held for World Youth Skills Day, the summit includes expert-led sessions on artificial intelligence, corporate careers, higher education, social impact, entrepreneurship, and freelancing. Participants receive a digital certificate; registration is free.'),

    ('JobSpecs 2026', 'Business', 'Duration: 2 days (exact date to be announced)', 'Khulna University of Engineering & Technology, Khulna',
     'A national job fair connecting students and graduates with leading employers, recruiters, and career-development sessions.',
     NULL, 'Khulna University of Engineering & Technology (KUET)', 'Job Fair', 'PUBLISHED',
     'Organizer: Spectrum. Spectrum''s flagship national job fair offers recruiter booths, full-time and internship or management-trainee openings, walk-in interviews, on-the-spot assessments, and sessions on CV writing, interview preparation, and industry trends. Attendees are encouraged to bring resumes.'),

    ('Beyond Algorithms: Machine Learning & Deep Learning', 'Workshop', '5 August 2026, 4:00 PM', 'CSE Seminar Room, RUET, Rajshahi',
     'An introductory session on ML and DL fundamentals, real-world applications, and future impact.',
     NULL, 'Rajshahi University of Engineering & Technology (RUET)', 'Seminar', 'PUBLISHED',
     'Organizers: IEEE RUET Student Branch and IEEE Computer Society RUET Student Branch Chapter. This joint beginner-friendly session explains how ML and DL technologies work, their applications and future impact, and the core principles behind intelligence systems. Registration is required via Google Form. Contacts: Adib Hassan and Abrar Munif.'),

    ('Colours of Revolution 3.0', 'Arts', '3 August 2026, 12:00 PM', 'NSU Gallery, Room NAC 106, North South University, Dhaka',
     'A live creative art competition celebrating artistic expression, courage, and change.',
     NULL, 'North South University (NSU)', 'Art Competition', 'PUBLISHED',
     'Organizer: NSU Art & Photography Club (NSUAPC). Participants create art live before judge Sunil Kumar. The organizers provide required art equipment and materials. Interested participants must complete the official registration form; instructions follow for confirmed entrants.'),

    ('THE ARCUS 1.0 - Regional Inter-University Competition', 'Competition', '3 August 2026', 'United International University (UIU), Dhaka',
     'A regional civil engineering championship featuring five design, engineering, and sustainability competition segments.',
     NULL, 'United International University (UIU)', 'Competition', 'PUBLISHED',
     'Organizer: UIU ACES (Association of Civil Engineering Students) / ARCUS. Sponsored by Victory Ideal Real Estate Ltd. and Shaheli Group, with Fantasy Kingdom Complex as entertainment partner. Segments: Arcography, ArcSpan, ArcTitron, Arc-Drift, and Arcology. Awards include champion and runner-up prizes, certificates, gifts, and media coverage.'),

    ('Industrial Cohort: AI Integrated Industrial Automation (SCADA, BMS & IIOT)', 'Workshop', '31 July 2026, 8:00 AM-5:00 PM (GMT+6)', 'Ulterior Engineering Intl., Joydebpur, Gazipur',
     'A hands-on industrial automation training program covering SCADA, BMS, IIOT, AI, a workshop, and an industrial tour.',
     NULL, 'North South University (NSU)', 'Training Program', 'PUBLISHED',
     'Organizer: IEEE NSU PES Student Branch Chapter, with Ulterior Engineering. Eligibility: EEE students with active IEEE membership and at least 60 completed credits; higher-credit applicants are prioritized. Selection includes application review and registration-fee confirmation. Full-day participants receive a certificate. Registration deadline: 25 July 2026, 11:00 PM (GMT+6); seats are limited.'),

    ('Skill Development Venture 5.0', 'Workshop', 'Starting 2 August 2026', 'North South University, Dhaka',
     'A three-phase series of career-oriented technical and creative workshops, ending with a GenAI fireside chat.',
     NULL, 'North South University (NSU)', 'Workshop Series', 'PUBLISHED',
     'Organizers: IEEE NSU Student Branch (INSB) and IEEE NSU Robotics and Automation Society Student Branch Chapter. Workshops include AWS Cloud Practitioner, multi-LLM routing, MicroPython, Canva, HOMER Pro, full-stack development, Docker and Kubernetes, ESP32, and Adobe Illustrator. Registering for any phase includes the closing GenAI fireside chat led by Saif Ahmed.'),

    ('1st Decolonizing Law and Criminology Conference in Bangladesh', 'Culture', 'Conference: 2 August 2026', 'Daffodil International University, Dhaka',
     'Bangladesh''s first conference on decolonizing law, criminology, and justice, featuring a call for abstracts.',
     NULL, 'Daffodil International University (DIU)', 'Conference', 'PUBLISHED',
     'Organizer: International Institute of Law and Diplomacy (IILD). Theme: ''Unshackling the Mind: Decolonizing Law, Criminology, and Justice in Bangladesh.'' Scholars, researchers, academics, practitioners, and students are invited to submit abstracts and register through the official Google Form. Abstract submission deadline: 30 June 2026, 11:59 PM.'),

    ('DUNMUN 2026 (13th Edition)', 'Culture', '19-22 August 2026', 'TSC, University of Dhaka',
     'A four-day Model United Nations conference with committee sessions, workshops, crisis simulations, and social events.',
     NULL, 'University of Dhaka', 'Model United Nations Conference', 'PUBLISHED',
     'Organizer: Dhaka University Model United Nations Association (DUMUNA). Theme: ''Consolidating Effective Multilateralism by Upholding Unity in Diversity in an Evolving Global Landscape.'' The conference includes UNHRC, IOM, FATF, UNCLOS, CEDAW, ECOWAS, UNEA-8, UNSC, Extraordinary Summit on Petroleum, and International Press committees. Registration fee: BDT 3,899; roles include Delegate, Double Delegate, Executive Board Member, and Campus Envoy.');
