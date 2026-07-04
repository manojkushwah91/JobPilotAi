-- ============================================================================
-- JobPilot AI — Seed Prompt Templates
-- Version: V16
-- Description: Seed default prompt templates for AI features
-- ============================================================================

INSERT INTO prompt_templates (id, use_case, name, version, system_prompt, user_prompt_template, variables, model, temperature, max_tokens, is_active, created_at, updated_at)
VALUES
(
    uuid_generate_v7(),
    'resume_scoring',
    'ATS Resume Scorer',
    1,
    'You are an expert ATS resume scoring system. Analyze the provided resume against the job description. Return a JSON object with exactly: atsScore (0-100 integer), scoreBreakdown (object with keys: keywords, format, experience, education — each 0-100), missingKeywords (array of strings), strengths (array of strings), improvements (array of strings).',
    'Resume content:
{{resumeContent}}

Job Description:
{{jobDescription}}',
    '[]',
    'gpt-4',
    0.3,
    2000,
    true,
    now(),
    now()
),
(
    uuid_generate_v7(),
    'skill_gap',
    'Skill Gap Analyzer',
    1,
    'You are an expert skill gap analyst. Compare the candidate''s resume to the target role requirements. Return a JSON object with exactly: existingSkills (array of strings), missingSkills (array of strings), skillGaps (array of {skill, category, importance, learningResources}), summary (string describing overall fit).',
    'Resume:
{{resumeContent}}

Target Role:
{{targetRole}}',
    '[]',
    'gpt-4',
    0.3,
    2000,
    true,
    now(),
    now()
),
(
    uuid_generate_v7(),
    'job_matching',
    'Job Match Analyzer',
    1,
    'You are an expert job matching system. Compare the candidate''s resume against the job posting. Return a JSON object with exactly: matchScore (0-100 number), matchBreakdown (object), matchedSkills (array), missingSkills (array), recommendation (string).',
    'Resume:
{{resumeContent}}

Job:
{{jobContent}}',
    '[]',
    'gpt-4',
    0.3,
    2000,
    true,
    now(),
    now()
),
(
    uuid_generate_v7(),
    'cover_letter',
    'Cover Letter Generator',
    1,
    'You are a professional cover letter writer. Write a compelling, personalized cover letter. Use proper business letter format with date, salutation, body paragraphs, and closing. Match the requested tone. Return only the letter body text, no metadata or JSON.',
    'Write a {{tone}} cover letter for {{companyName}} for the position of {{jobTitle}}.
Recipient: {{recipientName}}',
    '[]',
    'gpt-4',
    0.7,
    1500,
    true,
    now(),
    now()
)
ON CONFLICT DO NOTHING;