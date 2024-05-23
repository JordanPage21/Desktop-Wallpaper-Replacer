# Desktop Wallpaper Replacer

The Desktop Wallpaper Replacer is a Java application that allows users to change their desktop wallpaper by searching for images based on a given search term. The application provides a graphical user interface (GUI) for users to input their search term and interact with the application.

## Features

- **Search for Wallpaper**: Users can enter a search term for the type of wallpaper they want.
- **Random Image Selection**: The application searches for a random image related to the search term.
- **Download Image**: Once an image is found, it is downloaded to the local machine.
- **Set Wallpaper**: The downloaded image is then set as the desktop wallpaper.
- **User Feedback**: After setting the wallpaper, users are prompted to indicate whether they like the selected image.
- **Retry Mechanism**: If the user does not like the image or if no image is found for the search term, they can input another search term to try again.

## How it Works

1. **Input Search Term**: Users input a search term for the type of wallpaper they want.
2. **Search for Image**: The application searches a predefined website for a random image related to the search term.
3. **Download Image**: Once an image is found, it is downloaded to the local machine.
4. **Set Wallpaper**: The downloaded image is set as the desktop wallpaper using the operating system's functionality.
5. **User Feedback**: Users are prompted to indicate whether they like the selected image.
6. **Retry or Exit**: If the user does not like the image or if no image is found, they can input another search term to try again, or exit the application.

## Configuration

The application uses environment variables for configuration. The following variables can be configured:

- `baseUrl`: The base URL of the website to search for images.
- `baseDir`: The base directory where downloaded images will be saved.

## Usage

To run the Desktop Wallpaper Replacer:

1. Ensure that the required environment variables (`baseUrl` and `baseDir`) are properly configured in the application.yaml.
2. Start the application by running the executable JAR file or launching it from an IDE.
3. The GUI will appear, prompting the user to input a search term.
4. Enter a search term and press the "Change Wallpaper" button or the enter key.
5. The application will search for a related image, download it, and set it as the desktop wallpaper.
6. Users will be prompted to indicate whether they like the selected image.
7. Based on the user's feedback, they can retry with another search term or exit the application.

## Technologies Used

- Java
- Spring Boot
- JavaFX
- Jsoup (for web scraping)
- JNA (Java Native Access)

## License

This project is licensed under the [MIT License](LICENSE).
