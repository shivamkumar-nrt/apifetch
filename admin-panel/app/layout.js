import "./globals.css";

export const metadata = {
  title: "APIForge Studio – Next-Generation API Platform",
  description: "Enterprise API testing, monitoring, load testing, and collaboration platform. Replace Postman, JMeter, and Swagger with one unified tool.",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400&display=swap" rel="stylesheet" />
      </head>
      <body>{children}</body>
    </html>
  );
}
