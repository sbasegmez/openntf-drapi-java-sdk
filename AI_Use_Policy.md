# AI Use Policy

This project permits the use of AI-assisted development tools under limited conditions as described below. As a general principle, AI is permitted, and even encouraged to improve developer productivity and software quality without replacing developer understanding, authorship, or responsibility.

## Permitted Uses

AI tools may be used for development assistance, including:

- Repetitive unit tests, test scaffolding where the intended behavior and assertions are produced, and understood.
- Producing or proofreading documentation (including this!), Javadoc, comments, test descriptions/messages.
- IDE autocomplete and code completion.
- Discussing/planning software architecture, abstractions, SDK design practices, and conceptual design.
- Code review, finding potential defects, edge cases, or maintainability issues.
- Automating build, test, and deployment processes.

Examples of tools currently used by the project include **Anthropic Claude** and **GitHub Copilot**.

## Code Generation

As a general rule, AI should not be used to generate substantial implementation code that is accepted into the project without the developer independently designing, understanding, and validating that implementation.

AI output must not be treated as authoritative or accepted merely because it appears correct.

AI cannot hold copyright, so the developer/contributor remains responsible for the code ownership, proper licensing and legal compliance. In particular, contributors are expected to:

- Fully understand the behavior and purpose of the code they submit.
- Review and validate AI-assisted suggestions before incorporating them.
- Apply the same level of code quality standards that would apply to manually written code.
- Make a best-effort assessment that submitted code has not been reproduced from copyrighted or otherwise improperly licensed material.

## Agentic or Substantial AI Code Generation

If AI agents or other generative tools are used in the future to produce substantial portions of implementation code, that use should be disclosed in the relevant commit and/or pull request.

The disclosure should identify, where appropriate:

- The AI tool used.
- The scope of the generated or substantially AI-assisted code.
- The extent of human review or modification.
- Any relevant limitations or considerations reviewers should be aware of.

Disclosure does not reduce the contributor's responsibility for the resulting code. AI-generated code must be understood, reviewed, tested, and maintained to the same standard as any other contribution.

Also, disclosure does not imply that the contribution will be accepted. The project maintainers retain the right to reject AI-assisted contributions that do not justify use of AI or do not meet the project's quality standards.
