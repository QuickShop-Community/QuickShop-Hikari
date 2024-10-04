# Contributing to QuickShop-Hikari

We welcome contributions to QuickShop-Hikari and appreciate your efforts to improve the project! Before getting started, please take a moment to review these guidelines to help streamline the process and ensure consistency across contributions.

## Project Structure

Below is an outline of the project structure for reference:

### Style Guidelines

To maintain a consistent codebase, we ask that you use the code style defined in the [QuickShop_Style.xml](QuickShop_Style.xml) file. You can import this file into your IntelliJ environment by following these steps:

1. Navigate to `File` -> `Settings` -> `Editor` -> `Code Style`.
2. Click on the gear icon and select `Import Scheme`.
3. Choose `IntelliJ IDEA code style XML` and select the `QuickShop_Style.xml` file located in the `.contributing/` folder.

This will ensure that your code adheres to the project's formatting rules.

#### 1. Final Obsession IntelliJ Plugin

We recommend using the **Final Obsession** plugin to ensure that all applicable local variables, and method parameters are declared `final` where appropriate. This helps maintain immutability, enhancing code safety and readability.

##### Installation:

1. In IntelliJ, go to `File` -> `Settings` -> `Plugins`.
2. Search for the plugin [Final Obsession](https://plugins.jetbrains.com/plugin/21687-final-obsession).
3. Click `Install`.
4. Restart IntelliJ to activate the plugin.

By using this plugin, you ensure that your code adheres to immutability standards when necessary.

#### 2. Lombok Plugin

We also recommend using the **Lombok** plugin to reduce boilerplate code, such as getters, setters, and constructors. Lombok helps make the code cleaner and easier to maintain.

##### Installation:

1. In IntelliJ, go to `File` -> `Settings` -> `Plugins`.
2. Search for the plugin [Lombok](https://plugins.jetbrains.com/plugin/6317-lombok).
3. Click `Install`.
4. Restart IntelliJ to activate the plugin.

Using Lombok will keep the codebase clean and reduce the amount of boilerplate.

## Contributor License Agreement (CLA)

Before submitting your first pull request, you must sign our Contributor License Agreement (CLA). This is required to ensure that we can freely use your contributions while maintaining the integrity of the project's licensing. This will pop up for your PR automatically.

## Licensing for Contributions

All new contributions to this project will be licensed under the [AGPLv3](https://www.gnu.org/licenses/agpl-3.0.html) license. By contributing, you agree that your contributions will also be licensed under this license.

## Best Practices for Branch Names

To help organize the development process, we follow specific conventions for branch names based on their purpose:

### Feature Branches
Feature branches are used for developing new features. Use the prefix `feature/` or `feat/`.

- **Example**: `feature/login-system` or `feat/payment-processing`.

### Bugfix Branches
Bugfix branches are used to fix bugs in the code. Use the prefix `bugfix/` or `fix/`.

- **Example**: `bugfix/header-styling` or `fix/form-validation`.

### Hotfix Branches
Hotfix branches are created directly from the production branch to fix critical bugs in the production environment. Use the prefix `hotfix/` or `hfix/`.

- **Example**: `hotfix/critical-security-issue` or `hfix/performance-patch`.

### Release Branches
Release branches are used to prepare for a new production release. They allow for last-minute adjustments and polishing. Use the prefix `release/`.

- **Example**: `release/v1.0.1`.

## How to Contribute

1. **Fork** the repository to your own GitHub account.
2. **Clone** the forked repository to your local machine.
3. **Create a new branch** for your changes using the branch naming guidelines above.
4. **Commit** your changes with clear and descriptive commit messages.
5. **Push** your changes to your fork.
6. **Create a pull request** (PR) with a detailed explanation of your changes and any relevant issues.

Please ensure that your pull request is up to date with the latest version of the `main` branch before submission.

## Code Review and Merging

After submitting your pull request, one of the maintainers will review your changes. You may be asked to make adjustments or provide further clarification. Once approved, your changes will be merged into the main repository.

Thank you for your contribution!

## Resources
- [License](https://www.gnu.org/licenses/agpl-3.0.html)
- [Style File](QuickShop_Style.xml)